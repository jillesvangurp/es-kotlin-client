package io.inbot.eskotlinwrapper

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.endsWith
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import kotlinx.coroutines.runBlocking
import org.elasticsearch.action.search.source
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.junit.jupiter.api.Test

class SearchTest : AbstractElasticSearchTest(indexPrefix = "search") {

    @Test
    fun `lets find some things`() {
        // lets put some documents in an index
        dao.bulk {
            index(randomId(), TestModel("the quick brown emu"))
            index(randomId(), TestModel("the quick brown fox"))
            index(randomId(), TestModel("the quick brown horse"))
            index(randomId(), TestModel("lorem ipsum"))
        }
        dao.refresh()

        // get SearchResults with our DSL
        val results = dao.search {
            // this is now the searchRequest, the index is already set correctly
            source(
                SearchSourceBuilder.searchSource()
                    .size(20)
                    .query(BoolQueryBuilder()
                        .must(MatchQueryBuilder("message", "quick")))
            )
        }

        // we put totalHits at the top level for convenience
        assert(results.totalHits).isEqualTo(results.searchResponse.hits.totalHits)
        results.mappedHits.forEach {
            // and we use jackson to deserialize the results
            assert(it.message).contains("quick")
        }
        // iterating twice is no problem
        results.mappedHits.forEach {
            assert(it.message).contains("quick")
        }
    }

    @Test
    fun `you can search by pasting json as a String from the Kibana dev console as well`() {
        dao.bulk {
            index(randomId(), TestModel("the quick brown emu"))
            index(randomId(), TestModel("the quick brown fox"))
            index(randomId(), TestModel("the quick brown horse"))
            index(randomId(), TestModel("lorem ipsum"))
        }
        dao.refresh()
        val keyWord = "quick"
        val results = dao.search {
            // sometimes it is nice to just paste a query you prototyped in the developer console
            // also, Kotlin has multi line strings so this stuff is actually readable
            // and you can use variables in them!
            source(
                """
{
  "size": 20,
  "query": {
    "match": {
      "message": "$keyWord"
    }
  }
}
            """
            )
        }
        assert(results.totalHits).isEqualTo(results.searchResponse.hits.totalHits)
        results.mappedHits.forEach {
            // and we use jackson to deserialize the results
            assert(it.message).contains("$keyWord")
        }
    }

    @Test
    fun `scrolling searches are easy`() {
        // lets put some documents in an index
        dao.bulk {
            for (i in 1..103) {
                index(randomId(), TestModel("doc $i"))
            }
        }
        dao.refresh()

        val results = dao.search {
            scroll(TimeValue.timeValueMinutes(1L))
            source(
                SearchSourceBuilder()
                    .query(matchAllQuery())
                    .size(5)
            )
        }

        val fetchedResultsSize = results.mappedHits.count().toLong()
        assert(fetchedResultsSize).isEqualTo(results.totalHits)
        assert(fetchedResultsSize).isEqualTo(103L)
    }

    @Test
    fun `scroll and bulk update works too`() {
        dao.bulk {
            for (i in 1..19) {
                index(randomId(), TestModel("doc $i"))
            }
        }
        dao.refresh()

        val queryForAll = SearchSourceBuilder()
            .query(matchAllQuery())
            // we need the version so that we can do bulk updates
            .seqNoAndPrimaryTerm(true)
            .size(5)
        val results = dao.search {
            scroll(TimeValue.timeValueMinutes(1L))
            source(queryForAll)
        }

        dao.bulk {
            results.hits.forEach { (searcHit, mapped) ->
                update(searcHit.id, searcHit.seqNo, searcHit.primaryTerm, mapped!!) {
                    it.message = "${it.message} updated"
                    it
                }
            }
        }

        dao.refresh()

        val updatedResults = dao.search {
            scroll(TimeValue.timeValueMinutes(1L))
            source(queryForAll)
        }
        assert(updatedResults.totalHits).isEqualTo(19L)
        updatedResults.mappedHits.forEach {
            assert(it.message).endsWith("updated")
        }
    }

    @Test
    fun `async search using coroutines`() {
        dao.bulk {
            index(randomId(), TestModel("the quick brown emu"))
            index(randomId(), TestModel("the quick brown fox"))
            index(randomId(), TestModel("the quick brown horse"))
            index(randomId(), TestModel("lorem ipsum"))
        }
        dao.refresh()
        runBlocking {
            assert(dao.searchAsync { }.totalHits).isGreaterThan(0)
        }
    }
}