package io.inbot.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest.AliasActions.Type
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.crudDao
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.writeAny
import org.elasticsearch.common.xcontent.xContent
import org.junit.jupiter.api.Test

class IndexManagementTest : AbstractElasticSearchTest("indexmngmnt", createIndex = false) {
    @Test
    fun `should list aliases`() {
        dao.createIndex {
            source(this::class.java.getResource("/testmodel-settings.json").readText(), XContentType.JSON)
        }

        val writeAlias = "${dao.indexName}_write"
        val readAlias = "${dao.indexName}_read"

        esClient.indices()
            .updateAliases(IndicesAliasesRequest()
                .addAliasAction(AliasActions(Type.ADD).index(dao.indexName).alias(writeAlias))
                .addAliasAction(AliasActions(Type.ADD).index(dao.indexName).alias(readAlias)), RequestOptions.DEFAULT)

        val dao2 = esClient.crudDao(
            index = dao.indexName,
            readAlias = readAlias,
            writeAlias = writeAlias,
            refreshAllowed = true,
            modelReaderAndWriter = JacksonModelReaderAndWriter(
                TestModel::class,
                ObjectMapper().findAndRegisterModules()
            )
        )
        val aliases = dao2.currentAliases()
        assertThat(aliases.size).isEqualTo(2)
        aliases.forEach {
            println(it.alias)
        }
    }



    @Test
    fun `create custom mapping`() {
        dao.createIndex {
            source(xContent(
                   mapOf("mapping" to mapOf(
                       "properties" to mapOf(
                           "title" to mapOf(
                               "type" to "text"))))))

        }
    }
}
