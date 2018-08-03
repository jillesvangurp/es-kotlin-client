package io.inbot.eskotlinwrapper

import org.elasticsearch.action.search.ClearScrollRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.search.SearchHit

interface SearchResults<T : Any> {
    val mappedHits: Sequence<T>
    val hits: Sequence<Pair<SearchHit, T?>>

    val totalHits: Long
    val searchResponse: SearchResponse
    val modelReaderAndWriter: ModelReaderAndWriter<T>
}

class PagedSearchResults<T : Any>(
    override val searchResponse: SearchResponse,
    override val modelReaderAndWriter: ModelReaderAndWriter<T>
) : SearchResults<T> {

    override val hits: Sequence<Pair<SearchHit, T?>>
        get() {
            return searchResponse.hits?.hits?.asSequence()?.map { Pair(it, modelReaderAndWriter.deserialize(it)) } ?: emptySequence()
        }
    override val mappedHits: Sequence<T>
        get() {
            return searchResponse.hits?.mapHits(modelReaderAndWriter) ?: emptySequence()
        }

    override val totalHits: Long
        get() {
            return searchResponse.hits?.totalHits ?: 0
        }
}

class ScrollingSearchResults<T : Any>(
    override val searchResponse: SearchResponse,
    override val modelReaderAndWriter: ModelReaderAndWriter<T>,
    val restHighLevelClient: RestHighLevelClient,
    val scrollTtlInMinutes: Long
) : SearchResults<T> {
    override val hits: Sequence<Pair<SearchHit, T?>>
        get() {
            return responses().flatMap { response ->
                response.hits?.asSequence()?.map { Pair(it, modelReaderAndWriter.deserialize(it)) } ?: emptySequence()
            }
        }

    override val mappedHits: Sequence<T>
        get() {
            return hits.map { it.second!! }
        }

    override val totalHits: Long
        get() {
            return searchResponse.hits?.totalHits ?: 0
        }

    private fun responses(): Sequence<SearchResponse> {
        return generateSequence(
            seed = searchResponse,
            nextFunction = {
                val currentScrollId = it.scrollId
                if (currentScrollId != null && it.hits.hits != null && it.hits.hits.size > 0) {
                    restHighLevelClient.searchScroll(
                        SearchScrollRequest(currentScrollId).scroll(
                            TimeValue.timeValueMinutes(
                                scrollTtlInMinutes
                            )
                        )
                    )
                } else {
                    val clearScrollRequest = ClearScrollRequest()
                    clearScrollRequest.addScrollId(currentScrollId)
                    restHighLevelClient.clearScroll(clearScrollRequest)
                    null
                }
            })
    }
}