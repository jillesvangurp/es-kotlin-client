package io.inbot.eskotlinwrapper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.bulkAsync
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus

private val logger = KotlinLogging.logger {}

/**
 * Asynchronous bulk indexing that uses the experimental Kotlin flows. Works similar to the synchronous version except
 * it fires bulk requests asynchronously on the specified dispatcher. On paper using multiple threads, allows ES to
 * use multiple Threads to consume bulk requests.
 *
 * Note: you need the kotlin.Experimental flag set for this to work. As Flow is a bit in flux, I expect the internals of this may change still before they finalize this. So, beware when using this.
 */
@Suppress("unused")
class AsyncBulkIndexingSession<T : Any> private constructor(
    private val dao: IndexDAO<T>,
    private val modelReaderAndWriter: ModelReaderAndWriter<T> = dao.modelReaderAndWriter,
    private val retryConflictingUpdates: Int = 0,
    private val operationChannel: SendChannel<BulkOperation<T>>,
    private val itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
    private val defaultRequestOptions: RequestOptions = dao.defaultRequestOptions
) {
    companion object {
        @ExperimentalCoroutinesApi
        private fun <T> chunkFLow(
            chunkSize: Int = 20,
            producerBlock: CoroutineScope.(channel: SendChannel<T>) -> Unit
        ): Flow<List<T>> =
            flow {
                coroutineScope {
                    val channel = Channel<T>(chunkSize)
                    launch {
                        producerBlock(channel)
                    }
                    var page = mutableListOf<T>()
                    channel.consumeEach { value ->
                        page.add(value)
                        if (page.size > chunkSize) {
                            emit(page)
                            page = mutableListOf()
                        }
                    }
                    if (page.isNotEmpty()) {
                        emit(page)
                    }
                }
            }

        @ExperimentalCoroutinesApi
        suspend fun <T : Any> asyncBulk(
            client: RestHighLevelClient,
            dao: IndexDAO<T>,
            modelReaderAndWriter: ModelReaderAndWriter<T> = dao.modelReaderAndWriter,
            bulkSize: Int = 100,
            retryConflictingUpdates: Int = 0,
            refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
            itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
            defaultRequestOptions: RequestOptions = dao.defaultRequestOptions,
            block: AsyncBulkIndexingSession<T>.() -> Unit,
            bulkDispatcher: CoroutineDispatcher?
        ) {
            val flow = chunkFLow<BulkOperation<T>>(chunkSize = bulkSize) { channel ->
                val session = AsyncBulkIndexingSession<T>(
                    dao,
                    modelReaderAndWriter,
                    retryConflictingUpdates,
                    channel,
                    itemCallback,
                    defaultRequestOptions
                )
                block.invoke(session)
                // close the channel so the collector doesn't wait forever
                channel.close()
            }.map { ops ->
                val bulkRequest = BulkRequest()
                bulkRequest.refreshPolicy = refreshPolicy
                ops.forEach { bulkRequest.add(it.operation) }
                client.bulkAsync(bulkRequest, defaultRequestOptions).items?.forEach {
                    val bulkOperation = ops[it.itemId]
                    bulkOperation.itemCallback.invoke(bulkOperation, it)
                }
            }
            // FIXME: figure out how to actually get this called in parallel. Currently it seems to do this sequentially
            // loads of confusing suggestions in the issue tracker for this with a lot of intentions and uncertainty around this and without a good way to do this NOW.
            // https://github.com/Kotlin/kotlinx.coroutines/issues/1147
            if (bulkDispatcher != null) {
                flow.flowOn(bulkDispatcher).collect {}
            } else {
                flow.collect {}
            }
        }
    }

    private fun defaultItemResponseCallback(operation: BulkOperation<T>, itemResponse: BulkItemResponse) {
        if (itemCallback == null) {
            if (itemResponse.isFailed) {
                if (retryConflictingUpdates > 0 && DocWriteRequest.OpType.UPDATE === itemResponse.opType && itemResponse.failure.status === RestStatus.CONFLICT) {
                    dao.update(operation.id, retryConflictingUpdates, defaultRequestOptions, operation.updateFunction!!)
                    logger.debug { "retried updating ${operation.id} after version conflict" }
                } else {
                    logger.warn { "failed item ${itemResponse.itemId} ${itemResponse.opType} on ${itemResponse.id} because ${itemResponse.failure.status} ${itemResponse.failureMessage}" }
                }
            }
        } else {
            itemCallback.invoke(operation, itemResponse)
        }
    }

    @Suppress("DEPRECATION") // we allow using types for now
    fun index(
        id: String,
        obj: T,
        create: Boolean = true,
        itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback
    ) {
        val indexRequest = IndexRequest()
            .index(dao.indexWriteAlias)
            .id(id)
            .create(create)
            .source(modelReaderAndWriter.serialize(obj), XContentType.JSON)
        if (!dao.type.isNullOrBlank()) {
            indexRequest.type(dao.type)
        }

        operationChannel.sendBlocking(
            BulkOperation(
                indexRequest,
                id,
                itemCallback = itemCallback
            )
        )
    }

    /**
     * Safe way to bulk update objects. Gets the object from the index first before applying the lambda to it to modify the existing object. If you set `retryConflictingUpdates` > 0, it will attempt to retry to get the latest document and apply the `updateFunction` if there is a version conflict.
     */
    fun getAndUpdate(
        id: String,
        itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback,
        updateFunction: (T) -> T
    ) {
        val pair = dao.getWithGetResponse(id)
        if (pair != null) {
            val (retrieved, resp) = pair
            update(id, resp.seqNo, resp.primaryTerm, retrieved, itemCallback, updateFunction)
        }
    }

    /**
     * Bulk update objects. If you have the object (e.g. because you are processing the sequence of a scrolling search), you can update what you have in a safe way.  If you set `retryConflictingUpdates` > 0, it will retry by getting the latest version and re-applying the `updateFunction` in case of a version conflict.
     */
    @Suppress("DEPRECATION") // we allow using types for now
    fun update(
        id: String,
        seqNo: Long,
        primaryTerms: Long,
        original: T,
        itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback,
        updateFunction: (T) -> T
    ) {
        val updateRequest = UpdateRequest()
            .index(dao.indexWriteAlias)
            .id(id)
            .detectNoop(true)
            .setIfSeqNo(seqNo)
            .setIfPrimaryTerm(primaryTerms)
            .doc(modelReaderAndWriter.serialize(updateFunction.invoke(original)), XContentType.JSON)
        if (!dao.type.isNullOrBlank()) {
            updateRequest.type(dao.type)
        }
        operationChannel.sendBlocking(
            BulkOperation(
                updateRequest,
                id,
                updateFunction = updateFunction,
                itemCallback = itemCallback
            )
        )
    }

    /**
     * Delete an object from the index.
     */
    @Suppress("DEPRECATION") // we allow using types for now
    fun delete(
        id: String,
        seqNo: Long? = null,
        term: Long? = null,
        itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback
    ) {
        val deleteRequest = DeleteRequest()
            .index(dao.indexWriteAlias)
            .id(id)

        if (!dao.type.isNullOrBlank()) {
            deleteRequest.type(dao.type)
        }

        if (seqNo != null) {
            deleteRequest.setIfSeqNo(seqNo)
            if (term != null) {
                deleteRequest.setIfPrimaryTerm(term)
            }
        }
        operationChannel.sendBlocking(
            BulkOperation(
                deleteRequest,
                id,
                itemCallback = itemCallback
            )
        )
    }
}