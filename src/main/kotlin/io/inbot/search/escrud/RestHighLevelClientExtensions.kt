package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.Header
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RestHighLevelClient

inline fun <T : Any> RestHighLevelClient.crudDao(index: String, modelReaderAndWriter: ModelReaderAndWriter<T>, refreshAllowed: Boolean = false): ElasticSearchCrudDAO<T> {
    return ElasticSearchCrudDAO(index, this, modelReaderAndWriter, refreshAllowed = refreshAllowed)
}


inline fun <reified T : Any> RestHighLevelClient.crudDao(index: String, objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules(), refreshAllowed: Boolean = false): ElasticSearchCrudDAO<T> {
    return ElasticSearchCrudDAO(index, this, JacksonModelReaderAndWriter(T::class,objectMapper), refreshAllowed = refreshAllowed)
}

fun RestHighLevelClient.doSearch(headers: List<Header> = listOf(), block: SearchRequest.() -> Unit): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return this.search(searchRequest,*headers.toTypedArray())
}
