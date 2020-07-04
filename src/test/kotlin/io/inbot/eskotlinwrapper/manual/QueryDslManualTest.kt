package io.inbot.eskotlinwrapper.manual

import com.jillesvangurp.kotlin4example.mdLink
import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import io.inbot.eskotlinwrapper.dsl.MatchQuery
import io.inbot.eskotlinwrapper.dsl.bool
import io.inbot.eskotlinwrapper.dsl.customQuery
import io.inbot.eskotlinwrapper.mapProps
import org.elasticsearch.action.search.dsl
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.configure
import org.elasticsearch.client.indexRepository
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
            configure {
                mappings {
                    text("title")
                }
            }
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

        val markdown = sourceRepository.md {

            +"""
                Elasticsearch has a Query DSL and the Java Rest High Level Client comes with a very expansive
                set of builders that you can use to programmatically construct queries. Of course builders are 
                something that you should avoid in Kotlin. 
                
                On this page we outline a few ways in which you can build queries both programmatically using the builders
                that come with the Java client, using json strings, and using our Kotlin DSL.
                
                We will use the same example as before in ${mdLink(searchPage.title, searchPage.fileName)}. 
                
                ## Java Builders
                
                The Java client comes with `org.elasticsearch.index.query.QueryBuilders` which provides static methods 
                to create builders for the various queries. This covers most but probably not all of the query DSL 
                but should cover most commonly used things.

            """

            blockWithOutput {
                val results = thingRepository.search {
                    source(
                        searchSource()
                            .size(20)
                            .query(
                                boolQuery()
                                    .must(matchQuery("title", "quick").boost(2.0f))
                                    .must(matchQuery("title", "brown"))
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
                    source(
                        searchSource().apply {
                            query(
                                boolQuery().apply {
                                    must().apply {
                                        add(matchQuery("title", "quick").boost(2.0f))
                                        add(matchQuery("title", "brown"))
                                    }
                                }
                            )
                        }
                    )
                }
                println("We found ${results.totalHits} results.")
            }

            +"""
                
                Using `apply` gets rid of the need to chain all the calls and it is a little better but still a little verbose. 
                
                ## Kotlin Search DSL
                
                To address this, this library provides a DSL that allows you to mix both type safe DSL constructs 
                and simple schema-less manipulation of maps. We'll show several versions of the same query above to
                show how this works.
                
                The example below uses the type safe way to set up the same query as before.
            """

            blockWithOutput {
                // more idomatic Kotlin using apply { ... }
                val results = thingRepository.search {
                    // SearchRequest.dsl is the extension function that allows us to use the dsl.
                    dsl {
                        // SearchDSL is passed to the block as this
                        // It extends our MapBackedProperties class
                        // This allows us to delegate properties to a MutableMap

                        // from is a property that is stored in the map
                        from = 0

                        // MapBackedProperties actually implements MutableMap
                        // and delegates to a simple MutableMap.
                        // so this works too: this["from"] = 0

                        // Unfortunately Maps have their own size property so we can't
                        // use that as a property name for the query size :-(
                        resultSize = 20
                        // this actually puts a key "size" in the map

                        // query is a function that takes an ESQuery instance
                        query =
                            // bool is a function that create a BoolQuery,
                            // which extends ESQuery, that is injected into the block
                            bool {
                                // BoolQuery has a function called must
                                // it also has filter, should, and mustNot
                                must(
                                    // it has a vararg list of ESQuery
                                    MatchQuery("title", "quick") {
                                        // match always needs a field and query
                                        // but boost is optional
                                        boost = 2.0
                                    },
                                    // but the block param is nullable and
                                    // defaults to null
                                    MatchQuery("title", "brown")
                                )
                            }
                    }
                }
                println("We found ${results.totalHits} results.")
            }
            +"""
                If you want to use it in schemaless mode or want to use things that aren't part of the DSL
                this is easy too.
            """

            blockWithOutput {
                // more idomatic Kotlin using apply { ... }
                val results = thingRepository.search {
                    // SearchRequest.dsl is the extension function that allows us to use the dsl.
                    dsl {
                        this["from"] = 0
                        this["size"] = 10
                        query =
                            // custom query constructs an object with an object inside
                            // as elasticsearch expects.
                            customQuery("bool") {
                                // the inner object is a MapBackedProperties instance
                                // which is a MutableMap<String,Any>
                                // so we can assign a list to the must key
                                this["must"] = listOf(
                                    // match is another customQuery
                                    customQuery("match") {
                                        // elasticsearch expects fieldName: object
                                        // so we use mapProps to construct and use
                                        // another MapBackedProperties
                                        this["title"] = mapProps {
                                            this["query"] = "quick"
                                            this["boost"] = 2.0
                                        }
                                    }.toMap(),
                                    customQuery("match") {
                                        this["title"] = mapProps {
                                            this["query"] = "brown"
                                        }
                                    }.toMap()
                                )
                            }
                    }
                }
                println("We found ${results.totalHits} results.")
            }

            +"""
                ## Extending the DSL
                
                The Elasticsearch DSL is huge and only a small part is covered in our Kotlin DSL so far. Using the DSL
                in schema-less mode allows you to work around this and you can of course mix both approaches.
                
                However, if you need something added to the DSL it is really easy to do this yourself. For example 
                this is the implementation of the match we use above. 
            """

            snippetFromSourceFile("src/main/kotlin/io/inbot/eskotlinwrapper/dsl/full-text-queries.kt", "MATCH_QUERY", wrap = true)

            +"""
                Writing your own EsQuery subclass should be straight-forward. Just extend `EsQuery` and write a function 
                that constructs it.
                
                The DSL is currently kind of experimental and very incomplete. I will add more to this over time.
            """
        }
        markdownPageWithNavigation(queryDslPage, markdown.value)
    }
}
