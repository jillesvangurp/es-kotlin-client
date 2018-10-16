package io.inbot.eskotlinwrapper

import org.elasticsearch.action.search.ClearScrollRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchScrollRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.search.SearchHit

interface SearchResults<T : Any> {
    val searchHits: Sequence<SearchHit>
    val mappedHits: Sequence<T>
        get() {
            return searchHits.map { modelReaderAndWriter.deserialize(it) }
        }
    val hits: Sequence<Pair<SearchHit, T?>>
        get() {
            return searchHits.map {
                Pair(it, modelReaderAndWriter.deserialize(it))
            }
        }

    val totalHits: Long
    val searchResponse: SearchResponse
    val modelReaderAndWriter: ModelReaderAndWriter<T>
}

class PagedSearchResults<T : Any>(
    override val searchResponse: SearchResponse,
    override val modelReaderAndWriter: ModelReaderAndWriter<T>
) : SearchResults<T> {

    override val searchHits: Sequence<SearchHit>
        get() {
            return searchResponse.hits?.hits?.asSequence() ?: emptySequence()
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

    override val searchHits: Sequence<SearchHit>
        get() {
            return responses().flatMap { response ->
                response.hits?.asSequence() ?: emptySequence()
            }
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
                    restHighLevelClient.scroll(
                        SearchScrollRequest(currentScrollId).scroll(
                            TimeValue.timeValueMinutes(
                                scrollTtlInMinutes
                            )
                        ), RequestOptions.DEFAULT
                    )
                } else {
                    val clearScrollRequest = ClearScrollRequest()
                    clearScrollRequest.addScrollId(currentScrollId)
                    restHighLevelClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT)
                    null
                }
            })
    }
}