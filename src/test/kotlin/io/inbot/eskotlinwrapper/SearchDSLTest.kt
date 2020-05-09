package io.inbot.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.eskotlinwrapper.dsl.MatchOperator
import io.inbot.eskotlinwrapper.dsl.MultiMatchType
import io.inbot.eskotlinwrapper.dsl.SearchDSL
import io.inbot.eskotlinwrapper.dsl.ZeroTermsQuery
import io.inbot.eskotlinwrapper.dsl.bool
import io.inbot.eskotlinwrapper.dsl.boosting
import io.inbot.eskotlinwrapper.dsl.match
import io.inbot.eskotlinwrapper.dsl.matchAll
import io.inbot.eskotlinwrapper.dsl.matchBoolPrefix
import io.inbot.eskotlinwrapper.dsl.matchPhrasePrefix
import io.inbot.eskotlinwrapper.dsl.multiMatch
import io.inbot.eskotlinwrapper.dsl.queryString
import org.elasticsearch.action.search.dsl
import org.junit.jupiter.api.Test

class SearchDSLTest : AbstractElasticSearchTest(indexPrefix = "search", createIndex = true) {
    val objectMapper = ObjectMapper()

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `should construct matchAll query`() {
        val s = SearchDSL()
        s.apply {
            resultSize = 10
            from = 0
            query = matchAll()
        }
        assertThat(s["from"]).isEqualTo(0)
        assertThat(s["query"] as Map<String, Any>).hasSize(1)
        assertThat((s["query"] as Map<String, Any>).keys).contains("match_all")
    }

    @Test
    fun `should construct bool`() {
        testQuery {
            query = bool {
                should(
                    match("title", "foo"),
                    match("title", "quick brown fox") {
                        // ESQuery is a MutableMap that modifies the underlying queryDetails
                        this["boost"] = 0.6
                    }
                )
            }
        }
    }

    @Test
    fun `boosting query`() {
        testQuery {
            query = boosting {
                positive = matchAll()
                negative = match("title", "nooo")
                negativeBoost = 0.1
            }
        }
    }

    @Test
    fun `match query`() {
        testQuery {
            query = match("title", "foo bar") {
                operator = MatchOperator.AND
                zeroTermsQuery = ZeroTermsQuery.none
            }
        }
    }

    @Test
    fun `match bool prefix query`() {
        testQuery {
            query = matchBoolPrefix("title", "foo bar") {
                operator = MatchOperator.OR
            }
        }
    }

    @Test
    fun `match phrase prefix query` () {
        testQuery {
            query = matchPhrasePrefix("title", "foo ba") {
                slop = 3
            }
        }
    }

    @Test
    fun `multi match query`() {
        testQuery {
            query = multiMatch("foo bar","title","description") {
                type = MultiMatchType.best_fields
                fuzziness = "AUTO"
            }
        }
    }

    @Test
    fun `query string query`() {
        testQuery {
            query = queryString("foo bar") {
                defaultField = "title"
                fuzziness = "AUTO"
            }
        }
    }

    @Test
    fun `simple query string query`() {
        testQuery {
            query = queryString("foo AND bar", "title", "description") {
                fuzziness = "AUTO"
            }
        }
    }

    private fun testQuery(block: SearchDSL.() -> Unit): SearchResults<TestModel> {
        // we test here that ES does not throw some kind of error and accepts the query without validation problems
        // we don't care about the results in this case
        // note we also don't test all parameters
        return repository.search {
            dsl(true, block)
        }
    }
}


//private fun testQuery(q: ESQuery, assertBlock: Assert<String>.() -> Unit) {
//    val dsl = SearchDSL()
//    dsl.query = q
//    val serialized = dsl.stringify(true)
//    println(serialized)
//
//    assertThat(serialized).run(assertBlock)
//}
