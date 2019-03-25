package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class AbstractElasticSearchTest(val indexPrefix: String = "test", val createIndex: Boolean = true, val deleteIndexAfterTest: Boolean = true) {
    lateinit var dao: IndexDAO<TestModel>
    lateinit var esClient: RestHighLevelClient
    lateinit var indexName: String

    @BeforeEach
    fun before() {
        // sane defaults
        esClient = RestHighLevelClient(port = 9999)
        // each test gets a fresh index
        indexName = "$indexPrefix-" + randomId()

        dao = esClient.crudDao(
            index = indexName,
            refreshAllowed = true,
            modelReaderAndWriter = JacksonModelReaderAndWriter(
                TestModel::class,
                ObjectMapper().findAndRegisterModules()
            )
        )

        if (createIndex) {
            val settings = this.javaClass.getResource("/testmodel-settings.json").readText()
            dao.createIndex() {
                source(settings, XContentType.JSON)
            }
        }
    }

    @AfterEach
    fun after() {
        // delete the index after the test
        if (deleteIndexAfterTest) {
            dao.deleteIndex()
        }
    }
}
