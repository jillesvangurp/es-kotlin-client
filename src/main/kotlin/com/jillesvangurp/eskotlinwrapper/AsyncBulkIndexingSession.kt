package com.jillesvangurp.eskotlinwrapper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
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

data class AsyncBulkOperation<T : Any>(
        val operation: DocWriteRequest<*>,
        val id: String?,
        val updateFunction: suspend ((T) -> T) = { it },
        val itemCallback: suspend (AsyncBulkOperation<T>, BulkItemResponse) -> Unit
)

/**
 * Asynchronous bulk indexing that uses the experimental Kotlin flows. Works similar to the synchronous version except
 * it fires bulk requests asynchronously. On paper using multiple threads, allows ES to
 * use multiple Threads to consume bulk requests.
 *
 * Note: you need the kotlin.Experimental flag set for this to work. As Flow is a bit in flux, I expect the internals of this may change still before they finalize this. So, beware when using this.
 *
 */
@Suppress("unused")
class AsyncBulkIndexingSession<T : Any> constructor(
        private val repository: AsyncIndexRepository<T>,
        private val modelReaderAndWriter: ModelReaderAndWriter<T> = repository.modelReaderAndWriter,
        private val retryConflictingUpdates: Int = 0,
        private val operationChannel: SendChannel<AsyncBulkOperation<T>>,
        private val itemCallback: suspend ((AsyncBulkOperation<T>, BulkItemResponse) -> Unit),
        private val defaultRequestOptions: RequestOptions = repository.defaultRequestOptions
) {
    companion object {
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
                        channel.consumeAsFlow().collect { value ->
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

        suspend fun <T : Any> asyncBulk(
                client: RestHighLevelClient,
                repository: AsyncIndexRepository<T>,
                modelReaderAndWriter: ModelReaderAndWriter<T> = repository.modelReaderAndWriter,
                bulkSize: Int = 100,
                retryConflictingUpdates: Int = 0,
                refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
                itemCallback: suspend ((AsyncBulkOperation<T>, BulkItemResponse) -> Unit) = { operation, itemResponse ->
                    if (itemResponse.isFailed) {
                        if (retryConflictingUpdates > 0 && DocWriteRequest.OpType.UPDATE === itemResponse.opType && itemResponse.failure.status === RestStatus.CONFLICT) {
                            repository.update(operation.id ?: error("id is required for update"), retryConflictingUpdates, defaultRequestOptions, operation.updateFunction)
                            logger.debug { "retried updating ${operation.id} after version conflict" }
                        } else {
                            logger.warn { "failed item ${itemResponse.itemId} ${itemResponse.opType} on ${itemResponse.id} because ${itemResponse.failure.status} ${itemResponse.failureMessage}" }
                        }
                    }
                },
                defaultRequestOptions: RequestOptions = repository.defaultRequestOptions,
                block: suspend AsyncBulkIndexingSession<T>.() -> Unit,
                bulkDispatcher: CoroutineDispatcher?
        ) {
            val flow = chunkFLow<AsyncBulkOperation<T>>(chunkSize = bulkSize) { channel ->
                val session = AsyncBulkIndexingSession(
                        repository,
                        modelReaderAndWriter,
                        retryConflictingUpdates,
                        channel,
                        itemCallback,
                        defaultRequestOptions
                )
                launch {
                    // FIXME specify dispatcher?
                    val asyncJob = async {
                        block.invoke(session)
                    }
                    asyncJob.await()
                    // close the channel so the collector doesn't wait forever
                    channel.close()
                }
            }.map { ops ->
                val bulkRequest = BulkRequest()
                bulkRequest.refreshPolicy = refreshPolicy
                ops.forEach { bulkRequest.add(it.operation) }
                client.bulkAsync(bulkRequest, defaultRequestOptions).items?.forEach {
                    val bulkOperation = ops[it.itemId]
                    bulkOperation.itemCallback.invoke(bulkOperation, it)
                }
            }
            if (bulkDispatcher != null) {
                flow.flowOn(bulkDispatcher).collect {}
            } else {
                flow.collect {}
            }
        }
    }

    @Suppress("DEPRECATION") // we allow using types for now
    fun index(
            id: String?,
            obj: T,
            create: Boolean = true
    ) {
        val indexRequest = IndexRequest()
                .index(repository.indexWriteAlias)
                .source(modelReaderAndWriter.serialize(obj), XContentType.JSON)
                .let {
                    if (id == null) {
                        it
                    } else {
                        it.id(id).create(create)
                    }
                }
        if (!repository.type.isNullOrBlank()) {
            indexRequest.type(repository.type)
        }

        operationChannel.sendBlocking(
                AsyncBulkOperation(
                        operation = indexRequest,
                        id = id,
                        itemCallback = itemCallback
                )
        )
    }

    /**
     * Safe way to bulk update objects. Gets the object from the index first before applying the lambda to it to modify the existing object. If you set `retryConflictingUpdates` > 0, it will attempt to retry to get the latest document and apply the `updateFunction` if there is a version conflict.
     */
    suspend fun getAndUpdate(
            id: String,
            updateFunction: suspend (T) -> T
    ) {
        val pair = repository.getWithGetResponse(id)
        if (pair != null) {
            val (retrieved, resp) = pair
            update(id, resp.seqNo, resp.primaryTerm, retrieved, updateFunction)
        }
    }

    /**
     * Bulk update objects. If you have the object (e.g. because you are processing the sequence of a scrolling search), you can update what you have in a safe way.  If you set `retryConflictingUpdates` > 0, it will retry by getting the latest version and re-applying the `updateFunction` in case of a version conflict.
     */
    @Suppress("DEPRECATION") // we allow using types for now
    suspend fun update(
            id: String,
            seqNo: Long,
            primaryTerms: Long,
            original: T,
            updateFunction: suspend (T) -> T
    ) {
        val updateRequest = UpdateRequest()
                .index(repository.indexWriteAlias)
                .id(id)
                .detectNoop(true)
                .setIfSeqNo(seqNo)
                .setIfPrimaryTerm(primaryTerms)
                .doc(modelReaderAndWriter.serialize(updateFunction.invoke(original)), XContentType.JSON)
        if (!repository.type.isNullOrBlank()) {
            updateRequest.type(repository.type)
        }
        operationChannel.sendBlocking(
                AsyncBulkOperation(
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
            term: Long? = null
    ) {
        val deleteRequest = DeleteRequest()
                .index(repository.indexWriteAlias)
                .id(id)

        if (!repository.type.isNullOrBlank()) {
            deleteRequest.type(repository.type)
        }

        if (seqNo != null) {
            deleteRequest.setIfSeqNo(seqNo)
            if (term != null) {
                deleteRequest.setIfPrimaryTerm(term)
            }
        }
        operationChannel.sendBlocking(
                AsyncBulkOperation(
                        deleteRequest,
                        id,
                        itemCallback = itemCallback
                )
        )
    }
}
