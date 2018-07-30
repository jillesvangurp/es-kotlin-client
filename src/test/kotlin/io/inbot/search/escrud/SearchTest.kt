package io.inbot.search.escrud

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.junit.jupiter.api.Test

class SearchTest : AbstractElasticSearchTest() {

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
}