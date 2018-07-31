package io.inbot.eskotlinwrapper

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.matchAllQuery
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.junit.jupiter.api.Test

class SearchTest : AbstractElasticSearchTest(indexPrefix = "search") {

    @Test
    fun shouldFindStuff() {
        // lets put some documents in an index
        dao.bulk {
            index(randomId(), TestModel("the quick brown emu"))
            index(randomId(), TestModel("the quick brown fox"))
            index(randomId(), TestModel("the quick brown horse"))
            index(randomId(), TestModel("lorem ipsum"))
        }
        dao.refresh()

        // create a query (TODO make this nicer)
        val query = SearchSourceBuilder.searchSource()
            .size(20)
            .query(BoolQueryBuilder().must(MatchQueryBuilder("message", "quick")))

        // get SearchResults with our DSL
        val results = dao.search {
            // this is now the searchRequest, the index is already set correctly
            source(query)
        }

        // we put totalHits at the top level for convenience
        assert(results.totalHits).isEqualTo(results.searchResponse.hits.totalHits)
        results.hits.forEach {
            // and we use jackson to deserialize the results
            assert(it.message).contains("quick")
        }
        // iterating twice is no problem
        results.hits.forEach {
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
            source("""
{
  "size": 20,
  "query": {
    "match": {
      "message": "$keyWord"
    }
  }
}
            """)
        }
        assert(results.totalHits).isEqualTo(results.searchResponse.hits.totalHits)
        results.hits.forEach {
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
            val searchSourceBuilder = SearchSourceBuilder()
            searchSourceBuilder.query(matchAllQuery())
            searchSourceBuilder.size(5)
            source(searchSourceBuilder)
        }

        val fetchedResultsSize = results.hits.count().toLong()
        assert(fetchedResultsSize).isEqualTo(results.totalHits)
        assert(fetchedResultsSize).isEqualTo(103L)
    }
}