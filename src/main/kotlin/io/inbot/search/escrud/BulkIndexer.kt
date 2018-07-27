package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

private val logger = KotlinLogging.logger {}

class BulkIndexer<T : Any>(
    val client: RestHighLevelClient,
    val elasticSearchCrudDAO: ElasticSearchCrudDAO<T>,
    val objectMapper: ObjectMapper,
    val bulkSize: Int = 100
) : AutoCloseable {

    val page = ConcurrentLinkedDeque<DocWriteRequest<*>>()
    val rwLock = ReentrantReadWriteLock()

    fun index(id: String, obj: T, create: Boolean = true) {
        val indexRequest = IndexRequest()
            .create(create)
            .index(elasticSearchCrudDAO.index)
            .type(elasticSearchCrudDAO.index)
            .id(id)
            .source(objectMapper.writeValueAsBytes(obj), XContentType.JSON)
        rwLock.read { page.add(indexRequest) }
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