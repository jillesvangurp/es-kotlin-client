package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

class BulkIndexer<T : Any>(
    val client: RestHighLevelClient,
    val dao: ElasticSearchCrudDAO<T>,
    val objectMapper: ObjectMapper,
    val bulkSize: Int = 100
) : AutoCloseable { // autocloseable so we flush all the items ...

    // adding things to a request should be thread safe
    val page = ConcurrentLinkedDeque<DocWriteRequest<*>>()

    // we use rw lock to protect the current page. read here means using the list (to add stuff), write means  building the bulk request and clearing the list.
    val rwLock = ReentrantReadWriteLock()

    fun index(id: String, obj: T, create: Boolean = true) {
        val indexRequest = IndexRequest()
            .index(dao.index)
            .type(dao.type)
            .id(id)
            .create(create)
            .source(objectMapper.writeValueAsBytes(obj), XContentType.JSON)
        rwLock.read { page.add(indexRequest) }
        flushIfNeeded()
    }

    fun getAndUpdate(id: String,  updateFunction: (T)->T) {
        val pair = dao.getWithVersion(id)
        if(pair != null) {
            update(id, pair.second, pair.first, updateFunction)
        }
    }

    fun update(id: String, version: Long, original: T, updateFunction: (T)->T) {
        // FIXME we need some callback mechanism on flush to handle version conflicts and retry updates such that we don't overwrite stuff
        val updateRequest = UpdateRequest()
            .index(dao.index)
            .type(dao.type)
            .id(id)
            .detectNoop(true)
            .version(version)
            .doc(objectMapper.writeValueAsBytes(updateFunction.invoke(original)), XContentType.JSON)
        rwLock.read { page.add(updateRequest) }
        flushIfNeeded()
    }

    fun delete(id: String, version: Long?=null) {
        val deleteRequest = DeleteRequest()
            .index(dao.index)
            .type(dao.type)
            .id(id)
        if(version != null) {
            deleteRequest.version(version)
        }
        rwLock.read { page.add(deleteRequest) }
        flushIfNeeded()
    }

    fun flushIfNeeded() {
        if (page.size >= bulkSize) {
            flush()
        }
    }

    fun flush() {
        if (page.size > 0) {
            // make sure nobody writes to the list
            val bulkRequest = BulkRequest()
            rwLock.write {
                // check if some other thread did not beat us
                if (page.size > 0) {
                    logger.debug { "flushing ${page.size} items" }
                    page.forEach { bulkRequest.add(it) }
                    page.clear()
                }
            }
            client.bulk(bulkRequest)
        }
    }

    override fun close() {
        flush()
    }
}