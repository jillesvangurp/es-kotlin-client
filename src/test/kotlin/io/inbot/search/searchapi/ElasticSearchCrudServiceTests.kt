package io.inbot.search.searchapi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudDAO
import org.apache.http.HttpHost
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

fun randomId() = UUID.randomUUID().toString()

data class Foo(val message: String)
class ElasticSearchCrudServiceTests {
    lateinit var dao: ElasticSearchCrudDAO<Foo>

    @BeforeEach
    fun before() {
        val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
        val esClient = RestHighLevelClient(restClientBuilder)
        val objectMapper = ObjectMapper().findAndRegisterModules()
        dao = ElasticSearchCrudDAO<Foo>("test", Foo::class, esClient, objectMapper, refreshAllowed = true)
    }

    @Test
    fun `should index`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        assert(dao.get(id)).isEqualTo(Foo("hi"))
        dao.delete(id)
        assert(dao.get(id)).isNull()
    }

    @Test
    fun `should update`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.update(id) { Foo("bye") }
        assert(dao.get(id)!!.message).isEqualTo("bye")
    }

    @Test
    fun `should produce version conflict`() {
        val id = randomId()
        dao.index(id, Foo("hi"))

        assertThrows<ElasticsearchStatusException> {
            dao.index(id, Foo("bar"), create = false, version = 3)
        }
    }

    @Test
    fun `should do concurrent updates`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        // do some concurrent updates; without retries this will fail
        0.rangeTo(30).toList().parallelStream().forEach { n ->
            dao.update(id, { Foo("nr_$n")})
        }
    }

    @Test
    fun `should bulk index`() {
        dao.bulk(bulkSize = 2) {
            index("1", Foo("hi"))
            index("2", Foo("world"))
            index("3", Foo("."))
            index("4", Foo("!"),create = false)
            index("4", Foo("!!"))
            index("4", Foo("?"),create = true)
            index("5", Foo("and good bye"))
        }
        dao.refresh()
        assert(dao.get("2")!!.message).isEqualTo("world")
        assert(dao.get("4")!!.message).isEqualTo("!") // ! overwrote . but ? and !! failed because create was set to true
        assert(dao.get("5")).isNotNull() // last item was processed
    }
}
