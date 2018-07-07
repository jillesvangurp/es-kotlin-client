package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import kotlin.reflect.KClass

class ElasticSearchCrudDAO<T : Any>(
    val index: String,
    val clazz: KClass<T>,
    val client: RestHighLevelClient,
    val objectMapper: ObjectMapper
) {

    fun index(id: String, obj: T, create: Boolean = true, version: Long?=null) {
        val indexRequest = IndexRequest()
            .index(index)
            .type(index)
            .id(id)
            .create(create)
            .source(objectMapper.writeValueAsString(obj), XContentType.JSON)
        if(version != null) {
            indexRequest.version(version)
        }
        client.index(
            indexRequest
        )
    }

    fun update(id: String, transformFunction: (T) -> T) {
        val response = client.get(GetRequest().index(index).type(index).id(id))
        val currentVersion = response.version

        val sourceAsBytes = response.sourceAsBytes
        if (sourceAsBytes != null) {
            val currentValue = objectMapper.readValue(sourceAsBytes, clazz.java)!!
            val transformed = transformFunction.invoke(currentValue);
            index(id,transformed,create=false,version=currentVersion)
        } else {
            throw IllegalStateException("id $id not found")
        }
    }

    fun delete(id: String) {
        client.delete(DeleteRequest().index(index).type(index).id(id))
    }

    fun get(id: String): T? {
        val response = client.get(GetRequest().index(index).type(index).id(id))
        val sourceAsBytes = response.sourceAsBytes

        if (sourceAsBytes != null) {
            return objectMapper.readValue(sourceAsBytes, clazz.java)
        }
        return null
    }
}