package io.inbot.search.searchapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudDAO
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import java.util.UUID

fun randomId() = UUID.randomUUID().toString()

data class Foo(val message: String)
class ElasticSearchCrudServiceTests : StringSpec({
    val esClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))

    val objectMapper = ObjectMapper().findAndRegisterModules()
    val dao = ElasticSearchCrudDAO<Foo>("test", Foo::class, esClient, objectMapper)
    val id = randomId()

    "should index" {
        dao.index(id, Foo("hi"))
        dao.get(id) shouldBe Foo("hi")
        dao.delete(id)
        dao.get(id) shouldBe null
    }

    "should update" {
        dao.index(id, Foo("hi"))
        dao.update(id) { Foo("bye") }
        dao.get(id)!!.message shouldBe "bye"
    }
})
