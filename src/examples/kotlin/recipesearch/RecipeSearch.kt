package recipesearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.inbot.eskotlinwrapper.AsyncIndexRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.client.healthAsync
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.File

class RecipeSearch(
    private val recipeRepository: AsyncIndexRepository<Recipe>,
    private val objectMapper: ObjectMapper
) {

    suspend fun deleteIndex() {
        recipeRepository.deleteIndex()
    }

    suspend fun createNewIndex() {
        recipeRepository.createIndex {
//            source(
//                """
//
//            """.trimIndent(), XContentType.JSON
//            )
        }
    }

    @ExperimentalCoroutinesApi // will hopefully stabilize with next version
    suspend fun indexExamples() {
        // use a small bulk size to illustrate how this can
        // work with potentially large amounts of files.
        recipeRepository.bulk(bulkSize = 3) {
            File("src/examples/resources/recipes")
                .listFiles { f -> f.extension == "json" }?.forEach {
                    val parsed = objectMapper.readValue<Recipe>(it.readText())
                    // use create=false to allow updates
                    index(parsed.sourceUrl, parsed, create = false)
                }
        }
    }

    suspend fun healthStatus(): ClusterHealthStatus {
        return recipeRepository.client.cluster().healthAsync(ClusterHealthRequest()).status
    }

    suspend fun search(query: String, from: Int, size: Int): SearchResponse<Recipe> {
        val results = recipeRepository.search {
            source(SearchSourceBuilder.searchSource().apply {
                from(from)
                size(size)
                query(
                    QueryBuilders.boolQuery().apply {
                        should().apply {
                            add(QueryBuilders.matchPhraseQuery("title", query).boost(2.0f))
                            add(QueryBuilders.matchQuery("title", query).boost(2.0f))
                            add(QueryBuilders.matchQuery("description", query))
                        }
                    }
                )
            })
        }.toSearchResponse()
        return results
    }
}

