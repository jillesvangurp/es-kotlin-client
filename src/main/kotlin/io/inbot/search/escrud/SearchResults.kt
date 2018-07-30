package io.inbot.search.escrud

import org.elasticsearch.action.search.SearchResponse

class SearchResults<T : Any>(val searchResponse: SearchResponse, val modelReaderAndWriter: ModelReaderAndWriter<T>) {
    val hits: Sequence<T>
        get() {
            return searchResponse.hits?.mapHits(modelReaderAndWriter) ?: emptySequence()
        }

    val totalHits: Long
        get() {
            return searchResponse.hits?.totalHits ?: 0
        }
}