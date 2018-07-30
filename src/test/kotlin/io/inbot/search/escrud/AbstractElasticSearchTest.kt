package io.inbot.search.escrud

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach

open class AbstractElasticSearchTest {
    lateinit var dao: ElasticSearchCrudDAO<TestModel>
    lateinit var esClient: RestHighLevelClient
//    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun before() {
        val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
        esClient = RestHighLevelClient(restClientBuilder)
//        objectMapper = ObjectMapper().findAndRegisterModules()
        // each test gets a fresh index
        dao = esClient.crudDao<TestModel>("test-" + randomId(), refreshAllowed = true)
    }

}
