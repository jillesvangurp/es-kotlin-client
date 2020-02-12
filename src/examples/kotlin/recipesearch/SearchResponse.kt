package recipesearch

import io.inbot.eskotlinwrapper.SearchResults

// lets not expose ES API
data class SearchResponse<T : Any>(val totalHits: Int, val items: List<T>) {
    constructor(searchResponse: SearchResults<T>) :
            this(
                searchResponse.totalHits.toInt(),
                searchResponse.mappedHits.toList()
            )
}

fun <T : Any> SearchResults<T>.toSearchResponse() =  SearchResponse(this)

