package io.inbot.eskotlinwrapper

import mu.KotlinLogging
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

class BulkIndexingSession<T : Any>(
    val client: RestHighLevelClient,
    val dao: IndexDAO<T>,
    val modelReaderAndWriter: ModelReaderAndWriter<T>,
    val bulkSize: Int = 100,
    val retryConflictingUpdates: Int = 0,
    val refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
    val itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null
) : AutoCloseable { // autocloseable so we flush all the items ...

    data class BulkOperation<T : Any>(
        val operation: DocWriteRequest<*>,
        val id: String,
        val updateFunction: ((T) -> T)? = null,
        val itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = { _, _ -> }
    )
    // adding things to a request should be thread safe
    private val page = mutableListOf<BulkOperation<T>>()
    private val closed = AtomicBoolean(false)

    // we use rw lock to protect the current page. read here means using the list (to add stuff), write means  building the bulk request and clearing the list.
    private val rwLock = ReentrantReadWriteLock()

    internal fun defaultItemResponseCallback(operation: BulkOperation<T>, itemResponse: BulkItemResponse) {
        if (itemCallback == null) {
            if (itemResponse.isFailed) {
                if (retryConflictingUpdates > 0 && DocWriteRequest.OpType.UPDATE === itemResponse.opType && itemResponse.failure.status === RestStatus.CONFLICT) {
                    dao.update(operation.id, retryConflictingUpdates, operation.updateFunction!!)
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
            .index(dao.index)
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
            update(id, pair.second.version, pair.first, itemCallback, updateFunction)
        }
    }

    fun update(id: String, version: Long, original: T, itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = this::defaultItemResponseCallback, updateFunction: (T) -> T) {
        if (closed.get()) {
            throw IllegalStateException("cannot add bulk operations after the BulkIndexingSession is closed")
        }
        val updateRequest = UpdateRequest()
            .index(dao.index)
            .type(dao.type)
            .id(id)
            .detectNoop(true)
            .version(version)
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
            .index(dao.index)
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
            val bulkResponse = client.bulk(bulkRequest)
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