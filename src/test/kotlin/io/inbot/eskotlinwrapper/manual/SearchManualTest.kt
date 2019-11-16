package io.inbot.eskotlinwrapper.manual

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import io.inbot.eskotlinwrapper.JacksonModelReaderAndWriter
import kotlinx.coroutines.InternalCoroutinesApi
import org.elasticsearch.action.search.source
import org.elasticsearch.client.crudDao
import org.elasticsearch.common.xcontent.XContentType
import org.junit.jupiter.api.Test

private data class Thing(val title: String)

@InternalCoroutinesApi
class SearchManualTest: AbstractElasticSearchTest(indexPrefix = "manual") {
    @Test
    fun `search manual`() {
        // we have to do this twice once for printing and once for using :-)
        val modelReaderAndWriter =
            JacksonModelReaderAndWriter(Thing::class, ObjectMapper().findAndRegisterModules())
        // Create a Data Access Object

        val thingDao = esClient.crudDao("things", modelReaderAndWriter, refreshAllowed = true)
        // make sure we get rid of the things index before running the rest of this
        thingDao.deleteIndex()
        val settings = """
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
                          }
                        }
                      }
                    }
                """
        thingDao.createIndex {
            source(settings, XContentType.JSON)
        }


        KotlinForExample.markdownPage(searchPage) {

            block(true) {
                // lets use a slightly different model class this time
                data class Thing(val title: String)
            }

            +"""
                And lets use these settings and mappings:
                
                ```
                $settings
                ```
            """
            block(false) {
                // for testing, it's useful to allow calling refresh. Note we disallow this by default because this
                // whould not be used in production.
                val thingDao = esClient.crudDao("things", modelReaderAndWriter, refreshAllowed = true)
            }

            +"""
                Lets index some documents to look for ...
            """

            block(true) {
                thingDao.bulk {
                    index("1", Thing("The quick brown fox"))
                    index("2", Thing("The quick brown emu"))
                    index("3", Thing("The quick brown gnu"))
                    index("4", Thing("Another thing"))
                    5.rangeTo(100).forEach {
                        index("$it", Thing("Another thing: $it"))
                    }
                }
                // force ES to commit everything to disk so search works right away
                thingDao.refresh()
            }

            +"""
                ## Doing a simple JSON search.
                
                
            """
            blockWithOutput {
                // a SearchRequest is created and passed into the block
                val results = thingDao.search {
                    // we can use templating
                    val text = "brown"
                    source("""
                        {
                            "query": {
                                "match": {
                                    "title": {
                                        "query": "$text"
                                    }
                                }
                            }
                        }
                    """.trimIndent())
                }
                println("Found ${results.totalHits}")

                // get the deserialized thing from the search response
                results.mappedHits.forEach {
                    println(it)
                }

                // we can also get the underlying SearchHit
                results.searchHits.first().apply {
                    println("Hit: ${id}\n${sourceAsString}")
                }

                // or we can get both as Pair
                results.hits.first().apply {
                    val (searchHit,deserialized) = this
                    println("Hit: ${searchHit.id} deserialized from\n ${searchHit.sourceAsString}\nto\n$deserialized")
                }
            }

            +"""
                ## Scrolling searches
                
                Elasticsearch has a notion of scrolling searches for retrieving large amounts of 
                documents from an index. Normally this works by keeping track of a scroll token and
                passing that to Elasticsearch to fetch subsequent pages of results.
                
                To make this easier and less tedious, the search method on the dao has a simpler solution.
            """

            blockWithOutput {
                // simply set scrolling to true
                val results = thingDao.search(scrolling = true) {
                    source("""
                        {
                            "size": 2,
                            "query": {
                                "match_all": {}
                            }
                        }
                    """.trimIndent())
                }

                // with size: 2, this will page through ten pages of results before stopping
                results.mappedHits.take(20).forEach {
                    println(it)
                }
                // after the block exits, the scroll is cleaned up with an extra request
            }
        }
    }
}