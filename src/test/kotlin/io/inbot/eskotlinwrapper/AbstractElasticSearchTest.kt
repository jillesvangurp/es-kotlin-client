package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class AbstractElasticSearchTest(val indexPrefix: String = "test", val deleteIndexAfterTest: Boolean = true) {
    lateinit var dao: IndexDAO<TestModel>
    lateinit var esClient: RestHighLevelClient
    lateinit var indexName: String

    @BeforeEach
    fun before() {
        val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
        esClient = RestHighLevelClient(restClientBuilder)
        // each test gets a fresh index
        indexName = "$indexPrefix-" + randomId()
        dao = esClient.crudDao(indexName, refreshAllowed = true, modelReaderAndWriter = JacksonModelReaderAndWriter(
            TestModel::class,
            ObjectMapper().findAndRegisterModules()
        )
        )
    }

    @AfterEach
    fun after() {
        // delete the index after the test
        if (deleteIndexAfterTest) {
            esClient.indices().delete(DeleteIndexRequest(indexName))
        }
    }
}
