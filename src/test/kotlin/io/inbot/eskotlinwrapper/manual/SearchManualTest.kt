@file:Suppress("unused")

package io.inbot.eskotlinwrapper.manual

import com.jillesvangurp.kotlin4example.mdLink
import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import org.elasticsearch.action.search.source
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.configure
import org.elasticsearch.client.indexRepository
import org.junit.jupiter.api.Test

class SearchManualTest : AbstractElasticSearchTest(indexPrefix = "manual") {
    private data class Thing(val title: String)
    @Test
    fun `search manual`() {
        // we have to do this twice once for printing and once for using :-)
        val thingRepository = esClient.indexRepository<Thing>("things", refreshAllowed = true)
        // make sure we get rid of the things index before running the rest of this
        thingRepository.deleteIndex()
        thingRepository.createIndex {
            configure { mappings { text("title") } }
        }

        val markdown = sourceRepository.md {

            block(true) {
                // lets use a slightly different model class this time
                data class Thing(val title: String)
            }

            +"""
                Lets index some documents to look for ...
            """

            block(true) {
                // force ES to commit everything to disk so search works right away
                thingRepository.bulk(refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE) {
                    index("1", Thing("The quick brown fox"))
                    index("2", Thing("The quick brown emu"))
                    index("3", Thing("The quick brown gnu"))
                    index("4", Thing("Another thing"))
                    5.rangeTo(100).forEach {
                        index("$it", Thing("Another thing: $it"))
                    }
                }
            }

            +"""
                ## Searching

            """
            blockWithOutput {
                // a SearchRequest is created and passed into the block
                val results = thingRepository.search {
                    // we can use Kotlin's string templating
                    val text = "brown"
                    source(
                        """
                        {
                            "query": {
                                "match": {
                                    "title": {
                                        "query": "$text"
                                    }
                                }
                            }
                        }
                        """.trimIndent()
                    )
                }
                println("Found ${results.totalHits}")

                // we can get the deserialized thing from the search response
                results.mappedHits.forEach {
                    // kotlin sequences are lazy; nothing is deserialized unless you use it
                    println(it)
                }

                // we can also get the underlying `SearchHit` that Elasticsearch returns
                results.searchHits.first().apply {
                    // this would be useful if we wanted to do some bulk updates
                    println("Hit: $id $seqNo $primaryTerm\n$sourceAsString")
                }

                // or we can get both as a `Pair`
                results.hits.first().apply {
                    val (searchHit, deserialized) = this
                    println("Hit: ${searchHit.id}:\n$deserialized")
                }
            }

            +"""
                We provide several alternative ways to query elasticsearch; including a Kotlin DSL. For documentation for that see ${mdLink(
                queryDslPage.title,
                queryDslPage.fileName
            )}
            """

            +"""
                ## Count
                
                We can also query just to get a document count.
            """
            blockWithOutput {
                println("The total number of documents is ${thingRepository.count()}")

                // like with search, we can pass in a JSON query
                val query = "quick"
                val count = thingRepository.count {
                    source(
                        """
                        {
                            "query": {
                                "match": {
                                    "title": {
                                        "query": "$query"
                                    }
                                }
                            }
                        }                        
                        """.trimIndent()
                    )
                }
                println("We found $count results matching $query")
            }

            +"""
                ## Scrolling searches
                
                Elasticsearch has a notion of scrolling searches for retrieving large amounts of 
                documents from an index. Normally this works by keeping track of a scroll token and
                passing that to Elasticsearch to fetch subsequent pages of results. Scrolling is useful if
                you want to process large amounts of results.
                
                To make scrolling easier and less tedious, the search method on the repository 
                has a simpler solution: simply set `scrolling` to `true`.
                 
                A classic use case for using scrolls is to bulk update your documents. You can do this as follows. 
            """

            blockWithOutput {
                thingRepository.bulk {
                    // simply set scrolling to true will allow us to scroll over the entire index
                    // this will scale no matter what the size of your index is. If you use
                    // scrolling, you can also set the ttl for the scroll (default is 1m)
                    val results = thingRepository.search(
                        scrolling = true,
                        scrollTtlInMinutes = 10
                    ) {
                        source(
                            """
                            {
                                "size": 10,
                                "query": {
                                    "match_all": {}
                                }
                            }
                            """.trimIndent()
                        )
                    }
                    results.hits.forEach { (hit, thing) ->
                        if (thing != null) {
                            // we dig out the meta data we need for optimistic locking
                            // from the search response
                            update(hit.id, hit.seqNo, hit.primaryTerm, thing) { currentThing ->
                                currentThing.copy(title = "updated thing")
                            }
                        }
                    }
                    // after the last page of results, the scroll is cleaned up
                }
            }
        }
        markdownPageWithNavigation(searchPage, markdown.value)
    }
}
