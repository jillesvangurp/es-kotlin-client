package io.inbot.search.searchapi

import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudDAO
import io.inbot.search.escrud.crudDao
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BulkIndexerTest {
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
    fun `should bulk index`() {
        val ids = mutableListOf<String>()
        for (i in 0..4) {
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
        assertk.assert(dao.get(ids[1])!!.message).isEqualTo("world")
        assertk.assert(dao.get(ids[3])!!.message).isEqualTo("!") // ! overwrote . but ? and !! failed because create was set to true
        assertk.assert(dao.get(ids[4])).isNotNull() // last item was processed
    }

    @Test
    fun `should do bulk update`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.bulk() {
            getAndUpdate(id) { Foo("${it.message} world!") }
        }
        dao.refresh()
        assertk.assert(dao.get(id)!!.message).isEqualTo("hi world!")
    }

    @Test
    fun `should retry bulk updates`() {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.update(id) { foo -> Foo("hi wrld") }
        val (doc, version) = dao.getWithVersion(id)!!
        dao.bulk(retryConflictingUpdates = 2) {
            // version here is wrong but we have retries set to 2 so it will recover
            update(id, version - 1, doc) { foo -> Foo("omg") }
        }
        assertk.assert(dao.get(id)!!.message).isEqualTo("omg")
    }

    @Test
    fun `should bulk delete`() {
        val ids = mutableListOf<String>()
        dao.bulk() {
            for (i in 0..4) {
                val id = randomId()
                ids.add(id)
                index(id, Foo(id))
            }
            ids.forEach { delete(it) }
        }
        dao.refresh()
        assertk.assert(dao.get(ids[0])).isEqualTo(null)
    }
}