package io.inbot.search.escrud

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

// use random ids so we don't have conflicting tests
fun randomId() = UUID.randomUUID().toString()

data class Foo(val message: String)
class ElasticSearchCrudServiceTests {
    lateinit var dao: ElasticSearchCrudDAO<Foo>

    @BeforeEach
    fun before() {
        val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
        val esClient = RestHighLevelClient(restClientBuilder)
        val objectMapper = ObjectMapper().findAndRegisterModules()
        // each test gets a fresh index
        dao = esClient.crudDao<Foo>("test-" + randomId(), refreshAllowed = true)
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
            dao.update(id, 10) { Foo("nr_$n") }
        }
    }
}
