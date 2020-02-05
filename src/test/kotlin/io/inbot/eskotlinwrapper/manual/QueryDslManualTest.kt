package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import org.elasticsearch.action.search.source
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.indexRepository
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders.boolQuery
import org.elasticsearch.index.query.QueryBuilders.matchQuery
import org.elasticsearch.search.builder.SearchSourceBuilder.searchSource
import org.junit.jupiter.api.Test

class QueryDslManualTest : AbstractElasticSearchTest(indexPrefix = "manual") {
    private data class Thing(val title: String)

    @Test
    fun `query dsl manual`() {
        // we have to do this twice once for printing and once for using :-)
        val thingRepository = esClient.indexRepository<Thing>("things", refreshAllowed = true)
        // make sure we get rid of the things index before running the rest of this
        thingRepository.deleteIndex()
        thingRepository.createIndex {
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
                                  "title": {
                                    "type": "text"
                                  }
                                }
                              }
                            }
                        """, XContentType.JSON
            )
        }
        thingRepository.bulk(refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE) {
            index("1", Thing("The quick brown fox"))
            index("2", Thing("The quick brown emu"))
            index("3", Thing("The quick brown gnu"))
            index("4", Thing("Another thing"))
            5.rangeTo(100).forEach {
                index("$it", Thing("Another thing: $it"))
            }
        }

        KotlinForExample.markdownPageWithNavigation(queryDslPage) {

            +"""
                Elasticsearch has a Query DSL and the Java Rest High Level Client comes with a very expansive
                set of builders that you can use to programmatically construct queries. Of course builders are 
                something that you should avoid in Kotlin. 
                
                On this page, we'll demonstrate how you can use this Java API effectively from Kotlin in a series of 
                examples that get us increasingly closer to a more idiomatic way of using Kotlin.
                
                Using the same example index as we used earlier:
            """

            blockWithOutput {
                val results = thingRepository.search {

                    source(
                        searchSource()
                            .size(20)
                            .query(
                                boolQuery()
                                    .must(matchQuery("title", "quick").boost(2.0f))
                                    .must(matchQuery("title","brown"))
                            )
                    )
                }
                println("We found ${results.totalHits} results.")
            }

            +"""
                This is unfortunately quite ugly from a Kotlin point of view. Lets see if we can clean that up a little.
            """
            blockWithOutput {

                // more idomatic Kotlin using apply { ... }
                val results = thingRepository.search {
                    source(searchSource().apply {
                        query(
                            boolQuery().apply {
                                must().apply {
                                    add(matchQuery("title", "quick").boost(2.0f))
                                    add(matchQuery("title", "brown"))
                                }
                            }
                        )
                    })
                }
                println("We found ${results.totalHits} results.")
            }

            +"""
                This is better but still a little verbose. To improve on this, a few extension functions can help.
            """

            blockWithOutput {

                // more idomatic Kotlin using apply { ... }
                val results = thingRepository.search {
                    // one of our extension functions gets rid of a bit of ugliness here
                    source {
                        query(
                            boolQuery().apply {
                                must().apply {
                                    add(matchQuery("title", "quick").boost(2.0f))
                                    add(matchQuery("title", "brown"))
                                }
                            }
                        )
                    }
                }
                println("We found ${results.totalHits} results.")
            }
        }
    }
}