package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractAsyncElasticSearchTest
import io.inbot.eskotlinwrapper.manual.recipesearch.Recipe
import kotlinx.coroutines.runBlocking
import org.elasticsearch.client.asyncIndexRepository
import org.elasticsearch.common.xcontent.XContentType
import org.junit.jupiter.api.Test
import java.io.File

class BuildingARealSearchEngine : AbstractAsyncElasticSearchTest(indexPrefix = "searchengine", createIndex = false) {

    @Test
    fun `article markdown`() {
        val recipeRepository = esClient.asyncIndexRepository<Recipe>("searchengine")
        runBlocking {

            // make sure we get rid of the things index before running the rest of this
            recipeRepository.deleteIndex()
            recipeRepository.createIndex {
                source(
                    """
                                {
                                  "settings": {
                                    "index": {
                                      "number_of_shards": 3,
                                      "number_of_replicas": 0,
                                      "blocks": {
                                        "read_only_allow_delete": "false"
                                      }
                                    }
                                  },
                                  "mappings": {
                                    "properties": {
                                      "name": {
                                        "type": "text"
                                      },
                                      "amount": {
                                        "type": "long"
                                      }
                                    }
                                  }
                                }
                            """, XContentType.JSON)
            }
            KotlinForExample.markdownPageWithNavigation(recipeSearchEnginePage) {
                +"""
                    The Elastic Search Kotlin Wrapper is designed to simplify writing production code that
                    interacts with Elasticsearch.
                    
                    The easiest way to demonstrate how it works is just showing it with a simple but realistic 
                    example. The code below is very loosely based on an example in the 
                    [Elastic examples repository](https://github.com/elastic/examples/tree/master/Search/recipe_search_java)
                    
                    I've borrowed the data from there. To make things interesting, we'll pretend we need co-routines 
                    because we want to integrate this into a proper asynchronous application server like for example 
                    ktor.
                    
                    ## Our data model
                    
                    Consider this simple example json file for chicken enchilladas:
                    
                """
                mdCodeBlock(File("example_data/recipes/homemade-chicken-enchiladas.json").readText(),"javascript")
                +"""
                    We can create a simple Kotlin data model to represent recipes like this:
                """
                snippetBlockFromClass(Recipe::class, "model classes")

                +"""
                    ## Creating an index
                    
                    
                """
            }
        }

    }
}