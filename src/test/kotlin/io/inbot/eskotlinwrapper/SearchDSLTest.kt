package io.inbot.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import org.elasticsearch.common.xcontent.stringify
import org.junit.jupiter.api.Test

class SearchDSLTest {
    @Test
    fun `should construct matchAll query`() {
        val s = SearchDSL()
        s.apply {
            resultSize = 10
            from = 0
            query(matchAll())
        }
        println(s.stringify(true))

        assertThat(s["from"]).isEqualTo(0)
        assertThat(s["query"] as Map<String, Any>).hasSize(1)
        assertThat((s["query"] as Map<String, Any>).keys).contains("match_all")
    }

    @Test
    fun `should construct bool`() {
        val s = SearchDSL()
        s.apply {

            query(bool {
                should(
                    match("title", "foo"),
                    match("title", "quick brown fox") {
                        // ESQuery is a MutableMap that modifies the underlying queryDetails
                        this["value"] = "bar"
                    }
                )
            })
        }
        println(s.stringify(true))
        assertThat(s.stringify()).contains("bar")
    }
}
