package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.BeforeEach

open class AbstractElasticSearchTest {
    lateinit var dao: IndexDAO<TestModel>
    lateinit var esClient: RestHighLevelClient

    @BeforeEach
    fun before() {
        val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
        esClient = RestHighLevelClient(restClientBuilder)
        // each test gets a fresh index
        dao = esClient.crudDao("test-" + randomId(), refreshAllowed = true, modelReaderAndWriter = JacksonModelReaderAndWriter(TestModel::class, ObjectMapper().findAndRegisterModules()))
    }
}
