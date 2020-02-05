package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.create
import org.elasticsearch.client.indexRepository
import org.elasticsearch.common.xcontent.XContentType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class AbstractElasticSearchTest(val indexPrefix: String = "test", val createIndex: Boolean = true, val deleteIndexAfterTest: Boolean = true) {
    lateinit var repository: IndexRepository<TestModel>
    lateinit var esClient: RestHighLevelClient
    lateinit var indexName: String

    @BeforeEach
    fun before() {
        // sane defaults
        esClient = create(port = 9999)
        // each test gets a fresh index
        indexName = "$indexPrefix-" + randomId()

        repository = esClient.indexRepository(
            index = indexName,
            refreshAllowed = true,
            modelReaderAndWriter = JacksonModelReaderAndWriter(
                TestModel::class,
                ObjectMapper().findAndRegisterModules()
            )
        )

        if (createIndex) {
            val settings = this.javaClass.getResource("/testmodel-settings.json").readText()
            repository.createIndex {
                source(settings, XContentType.JSON)
            }
        }
    }

    @AfterEach
    fun after() {
        // delete the index after the test
        if (deleteIndexAfterTest) {
            repository.deleteIndex()
        }
    }
}
