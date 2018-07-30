package io.inbot.search.escrud

import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.junit.jupiter.api.Test



class SearchTest : AbstractElasticSearchTest() {

    @Test
    fun shouldFindStuff() {
        dao.bulk {
            index(randomId(), TestModel("the quick brown emu"))
            index(randomId(), TestModel("the quick brown fox"))
            index(randomId(), TestModel("the quick brown horse"))
            index(randomId(), TestModel("lorem ipsum"))
        }
        dao.refresh()

        val builder = SearchSourceBuilder.searchSource()
        builder.size(20)
        builder.query(BoolQueryBuilder().must(MatchQueryBuilder("message", "quick")))

        val searchResponse = dao.search {
            source(builder)
        }

        println(searchResponse.hits.totalHits)
        searchResponse.hits.hits.map(dao.modelReaderAndWriter::deserialize).forEach {
            println(it?.message)
        }
    }

}