package io.inbot.eskotlinwrapper

import assertk.Assert
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.common.xcontent.stringify
import org.junit.jupiter.api.Test

class SearchDSLTest {
    val objectMapper = ObjectMapper()

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `should construct matchAll query`() {
        val s = SearchDSL()
        s.apply {
            resultSize = 10
            from = 0
            query(matchAll())
        }
        assertThat(s["from"]).isEqualTo(0)
        assertThat(s["query"] as Map<String, Any>).hasSize(1)
        assertThat((s["query"] as Map<String, Any>).keys).contains("match_all")
    }

    @Test
    fun `should construct bool`() {
        testQuery(bool {
            should(
                match("title", "foo"),
                match("title", "quick brown fox") {
                    // ESQuery is a MutableMap that modifies the underlying queryDetails
                    this["boost"] = 0.6
                }
            )
        }) {
            contains("should")
            contains("boost")
            contains("0.6")
        }
    }

    @Test
    fun `boosting query`() {
        testQuery(boosting {
            positive = matchAll()
            negative = match("title", "nooo")
        }) {
            contains("positive")
            contains("negative")
            contains("title")
            contains("nooo")
        }
    }


    @Test
    fun `match query`() {
        testQuery(match("title", "foo bar") {
            operator = MatchOperator.AND
            zero_terms_query = ZeroTermsQuery.none
        }) {
            contains("match")
            contains("title")
            contains("foo bar")
            contains("AND")
            contains("none")
        }
    }

    @Test
    fun `match bool prefix query`() {
        testQuery(matchBoolPrefix("title", "foo bar") {
            operator = MatchOperator.OR
        }) {
            contains("match_bool_prefix")
            contains("title")
            contains("foo bar")
            contains("OR")
        }
    }
}

private fun testQuery(q:ESQuery, assertBlock: Assert<String>.() -> Unit) {
    val dsl = SearchDSL()
    dsl.query(q)
    val serialized = dsl.stringify(true)
    println(serialized)
    assertThat(serialized).run(assertBlock)
}
