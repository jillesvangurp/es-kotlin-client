package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.apache.commons.lang3.RandomUtils
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

class ElasticSearchCrudDAO<T : Any>(
    val index: String,
    val clazz: KClass<T>,
    val client: RestHighLevelClient,
    val objectMapper: ObjectMapper,
    val maxUpdateTries: Int = 10,
    val refreshAllowed: Boolean = false,
    val type: String = index // default to using index as the type but allow user to override
) {

    fun index(id: String, obj: T, create: Boolean = true, version: Long? = null) {
        val indexRequest = IndexRequest()
            .index(index)
            .type(type)
            .id(id)
            .create(create)
            .source(objectMapper.writeValueAsString(obj), XContentType.JSON)
        if (version != null) {
            indexRequest.version(version)
        }
        client.index(
            indexRequest
        )
    }

    fun update(id: String, transformFunction: (T) -> T) {
        update(0, id, transformFunction)
    }

    private fun update(tries: Int, id: String, transformFunction: (T) -> T) {
        try {
            val response = client.get(GetRequest().index(index).type(index).id(id))
            val currentVersion = response.version

            val sourceAsBytes = response.sourceAsBytes
            if (sourceAsBytes != null) {
                val currentValue = objectMapper.readValue(sourceAsBytes, clazz.java)!!
                val transformed = transformFunction.invoke(currentValue)
                index(id, transformed, create = false, version = currentVersion)
                if (tries > 0) {
                    // if you start seeing this a lot, you have a lot of concurrent updates to the same thing; not good
                    logger.warn { "retry update $id succeeded after tries=$tries" }
                }
            } else {
                throw IllegalStateException("id $id not found")
            }
        } catch (e: ElasticsearchStatusException) {

            if (e.status().status == 409) {
                if ( tries < maxUpdateTries) {
                    // we got a version conflict, retry after sleeping a bit (without this failures are more likely
                    Thread.sleep(RandomUtils.nextLong(50, 500))
                    update(tries + 1, id, transformFunction)
                } else {
                    throw IllegalStateException("update of $id failed after $tries attempts")
                }
            }
        }
    }

    fun delete(id: String) {
        client.delete(DeleteRequest().index(index).type(type).id(id))
    }

    fun get(id: String): T? {
        return getWithVersion(id)?.first
    }

    fun getWithVersion(id: String): Pair<T,Long>? {
        val response = client.get(GetRequest().index(index).type(type).id(id))
        val sourceAsBytes = response.sourceAsBytes

        if (sourceAsBytes != null) {
            val deserialized = objectMapper.readValue(sourceAsBytes, clazz.java)

            return Pair(deserialized!!,response.version)
        }
        return null
    }


    fun bulk(bulkSize: Int = 100, operationsBlock: BulkIndexer<T>.(bulkAPIFacade: BulkIndexer<T>) -> Unit) {
        val indexer = bulkIndexer(bulkSize = bulkSize)
        // autocloseable so we flush all the items ...
        indexer.use {
            operationsBlock.invoke(indexer, indexer)
        }
    }

    fun bulkIndexer(bulkSize: Int = 100) = BulkIndexer(client, this, objectMapper, bulkSize)

    fun refresh() {
        if (refreshAllowed) {
            // calling this is not safe in production settings but highly useful in tests
            client.lowLevelClient.performRequest("POST", "/$index/_refresh")
        } else {
            throw UnsupportedOperationException("refresh is not allowed; you need to opt in by setting refreshAllowed to true")
        }
    }
}