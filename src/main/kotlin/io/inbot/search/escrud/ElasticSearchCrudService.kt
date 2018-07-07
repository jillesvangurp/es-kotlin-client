package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import kotlin.reflect.KClass

class ElasticSearchCrudService<T : Any>(
    val index: String,
    val clazz: KClass<T>,
    val client: RestHighLevelClient,
    val objectMapper: ObjectMapper
) {

    fun create(id: String, obj: T, create: Boolean = true) {
        client.index(
            IndexRequest().index(index).type(index).id(id).create(create).source(
                objectMapper.writeValueAsString(obj),
                XContentType.JSON
            )
        )
    }

    fun delete(id: String) {
        client.delete(DeleteRequest().index(index).type(index).id(id))
    }

    fun get(id: String): T? {
        val response = client.get(GetRequest().index(index).type(index).id(id))
        if (response != null) {
            val sourceAsBytes = response.sourceAsBytes
            if (sourceAsBytes != null)
                return objectMapper.readValue(sourceAsBytes, clazz.java)
        }
        return null
    }
}