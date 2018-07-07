package io.inbot.search.searchapi

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.search.escrud.ElasticSearchCrudService
import io.kotlintest.shouldBe
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.Test

class ElasticSearchCrudServiceTests {

    data class Foo(val message: String)

    @Test
    fun shouldIndex() {
        val esClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))

        val objectMapper = ObjectMapper().findAndRegisterModules()
        val crud = ElasticSearchCrudService<Foo>("test", Foo::class, esClient, objectMapper)

        crud.create("666", Foo("hi"))
        crud.get("666") shouldBe Foo("hi")
        crud.delete("666")
        crud.get("666") shouldBe null
    }
}
