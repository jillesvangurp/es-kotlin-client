package io.inbot.eskotlinwrapper

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
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

/**
 * Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO].
 *
 * ```
 * dao.bulk() {
 *   index("xxx",myObject)
 * }
 * ```
 *
 */
class BulkIndexingSession<T : Any>(
    private val client: RestHighLevelClient,
    private val dao: IndexDAO<T>,
    /** Defaults to the one configured on the dao. */
    private val modelReaderAndWriter: ModelReaderAndWriter<T> = dao.modelReaderAndWriter,
    /** override this to change the bulk page size (the number of items sent to ES with one request).*/
    private val bulkSize: Int = 100,
    /** the default [itemCallback] is capable of retrying updates. When retrying it will get the document and try again. The default for this is 0. */
    private val retryConflictingUpdates: Int = 0,
    /** refresh policy on bulk requests. The default is to wait for changes to become available. This is the safest option because it avoids the risk of filling queues on the cluster. Set it to IMMEDIATE to make things faster. */
    private val refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
    /** The bulk API returns a response that contains a per item response. This callback facilitates dealing with e.g. failures. The default implementation does logging and update retries. */
    private val itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
    /** Override request options if you need to. Defaults to those configured on the dao. */
    private val defaultRequestOptions: RequestOptions = dao.defaultRequestOptions

) : AutoCloseable { // autocloseable so we flush all the items ...

    data class BulkOperation<T : Any>(
        val operation: DocWriteRequest<*>,
        val id: String,
        val updateFunction: ((T) -> T)? = null,
        val itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = { _, _ -> }
    )
    // adding things to a request should be thread safe
    private val page = ConcurrentLinkedDeque<BulkOperation<T>>()
    private val closed = AtomicBoolean(false)

    // we use rw lock to protect the current page. read here means using the list (to add stuff), write means  building the bulk request and clearing the list.
    private val rwLock = ReentrantReadWriteLock()

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

    fun index(id: String, obj: T, create: Boolean = true, itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback) {
        if (closed.get()) {
            throw IllegalStateException("cannot add bulk operations after the BulkIndexingSession is closed")
        }
        val indexRequest = IndexRequest()
            .index(dao.indexWriteAlias)
            .type(dao.type)
            .id(id)
            .create(create)
            .source(modelReaderAndWriter.serialize(obj), XContentType.JSON)
        rwLock.read { page.add(
            BulkOperation(
                indexRequest,
                id,
                itemCallback = itemCallback
            )
        ) }
        flushIfNeeded()
    }

    fun getAndUpdate(id: String, itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback, updateFunction: (T) -> T) {
        val pair = dao.getWithGetResponse(id)
        if (pair != null) {
            val (retrieved, resp) = pair
            update(id, resp.seqNo, resp.primaryTerm, retrieved, itemCallback, updateFunction)
        }
    }

    fun update(id: String, seqNo: Long, primaryTerms: Long, original: T, itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback, updateFunction: (T) -> T) {
        if (closed.get()) {
            throw IllegalStateException("cannot add bulk operations after the BulkIndexingSession is closed")
        }
        val updateRequest = UpdateRequest()
            .index(dao.indexWriteAlias)
            .type(dao.type)
            .id(id)
            .detectNoop(true)
            .setIfSeqNo(seqNo)
            .setIfPrimaryTerm(primaryTerms)
            .doc(modelReaderAndWriter.serialize(updateFunction.invoke(original)), XContentType.JSON)
        rwLock.read { page.add(
            BulkOperation(
                updateRequest,
                id,
                updateFunction = updateFunction,
                itemCallback = itemCallback
            )
        ) }
        flushIfNeeded()
    }

    fun delete(id: String, version: Long? = null, itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback) {
        if (closed.get()) {
            throw IllegalStateException("cannot add bulk operations after the BulkIndexingSession is closed")
        }
        val deleteRequest = DeleteRequest()
            .index(dao.indexWriteAlias)
            .type(dao.type)
            .id(id)
        if (version != null) {
            deleteRequest.version(version)
        }
        rwLock.read { page.add(
            BulkOperation(
                deleteRequest,
                id,
                itemCallback = itemCallback
            )
        ) }
        flushIfNeeded()
    }

    private fun flushIfNeeded() {
        if (page.size >= bulkSize) {
            flush()
        }
    }

    private fun flush() {
        if (page.size > 0) {
            // make sure nobody writes to the list
            val pageClone = mutableListOf<BulkOperation<T>>()
            rwLock.write {
                // check if some other thread did not beat us
                if (page.size > 0) {
                    page.forEach { pageClone.add(it) }
                    page.clear()
                }
            }
            val bulkRequest = BulkRequest()
            bulkRequest.refreshPolicy = refreshPolicy
            pageClone.forEach { bulkRequest.add(it.operation) }
            logger.debug { "flushing ${page.size} items" }
            val bulkResponse = client.bulk(bulkRequest, defaultRequestOptions)
            if (bulkResponse != null) {
                bulkResponse.items.forEach {
                    val bulkOperation = pageClone[it.itemId]
                    bulkOperation.itemCallback.invoke(bulkOperation, it)
                }
            }
        }
    }

    override fun close() {
        closed.set(true) // stop accepting operations
        flush()
    }
}