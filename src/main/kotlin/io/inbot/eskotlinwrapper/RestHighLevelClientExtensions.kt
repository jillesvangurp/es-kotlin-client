package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.Header
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits

fun <T : Any> RestHighLevelClient.crudDao(index: String, modelReaderAndWriter: ModelReaderAndWriter<T>, refreshAllowed: Boolean = false): IndexDAO<T> {
    return IndexDAO(index, this, modelReaderAndWriter, refreshAllowed = refreshAllowed)
}

inline fun <reified T : Any> RestHighLevelClient.crudDao(index: String, objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules(), refreshAllowed: Boolean = false): IndexDAO<T> {
    return IndexDAO(
        index,
        this,
        JacksonModelReaderAndWriter(T::class, objectMapper),
        refreshAllowed = refreshAllowed
    )
}

fun RestHighLevelClient.doSearch(headers: List<Header> = listOf(), block: SearchRequest.() -> Unit): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return this.search(searchRequest, *headers.toTypedArray())
}

fun <T : Any> SearchHits.mapHits(fn: (SearchHit) -> T): List<T> {
    return this.hits.map(fn)
}

fun <T : Any> SearchHits.mapHits(modelReaderAndWriter: ModelReaderAndWriter<T>): Sequence<T> {
    return this.hits.asSequence().map({ it -> modelReaderAndWriter.deserialize(it) ?: throw IllegalStateException("hit has no source") })
}