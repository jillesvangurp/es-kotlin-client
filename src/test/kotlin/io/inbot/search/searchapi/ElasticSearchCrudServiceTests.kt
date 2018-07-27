package io.inbot.search.searchapi

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudDAO
import io.inbot.search.escrud.crudDao
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
        dao = esClient.crudDao<Foo>("test-"+ randomId(), refreshAllowed = true)
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
            dao.update(id, { Foo("nr_$n") })
        }
    }

    @Test
    fun `should bulk index`() {
        val ids= mutableListOf<String>()
        for(i in 0..4) {
            ids.add(randomId())
        }

        dao.bulk(bulkSize = 2) {
            index(ids[0], Foo("hi"))
            index(ids[1], Foo("world"))
            index(ids[2], Foo("."))
            index(ids[3], Foo("!"), create = false)
            index(ids[3], Foo("!!"))
            index(ids[3], Foo("?"), create = true)
            index(ids[4], Foo("and good bye"))
        }
        dao.refresh()
        assert(dao.get(ids[1])!!.message).isEqualTo("world")
        assert(dao.get(ids[3])!!.message).isEqualTo("!") // ! overwrote . but ? and !! failed because create was set to true
        assert(dao.get(ids[4])).isNotNull() // last item was processed
    }

    @Test
    fun `should do bulk update`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.bulk() {
            getAndUpdate(id, {Foo("${it.message} world!")})
        }
        dao.refresh()
        assert(dao.get(id)!!.message).isEqualTo("hi world!")
    }

    @Test
    fun `should bulk delete`() {
        val ids = mutableListOf<String>()
        dao.bulk() {
            for (i in 0..4) {
                val id = randomId()
                ids.add(id)
                index(id,Foo(id))
            }
            ids.forEach { delete(it) }
        }
        dao.refresh()
        assert(dao.get(ids[0])).isEqualTo(null)
    }
}
