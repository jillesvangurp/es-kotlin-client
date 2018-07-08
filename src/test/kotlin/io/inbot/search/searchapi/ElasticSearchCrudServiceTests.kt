package io.inbot.search.searchapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudDAO
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import org.apache.http.HttpHost
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import java.util.UUID

fun randomId() = UUID.randomUUID().toString()

data class Foo(val message: String)
class ElasticSearchCrudServiceTests : StringSpec({
    val esClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))
    val objectMapper = ObjectMapper().findAndRegisterModules()
    val dao = ElasticSearchCrudDAO<Foo>("test", Foo::class, esClient, objectMapper)

    "should index" {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.get(id) shouldBe Foo("hi")
        dao.delete(id)
        dao.get(id) shouldBe null
    }

    "should update" {
        val id = randomId()
        dao.index(id, Foo("hi"))
        dao.update(id) { Foo("bye") }
        dao.get(id)!!.message shouldBe "bye"
    }

    "should produce version conflict" {
        val id = randomId()
        dao.index(id, Foo("hi"))
        shouldThrow<ElasticsearchStatusException> {
            dao.index(id, Foo("bar"), create = false, version = 3)
        }
    }

    "should do concurrent updates" {
        val id = randomId()
        dao.index(id, Foo("hi"))
        // do some concurrent updates; without retries this will fail
        0.rangeTo(30).toList().parallelStream().forEach { n ->
            dao.update(id, { Foo("nr_$n")})
        }
    }
})
