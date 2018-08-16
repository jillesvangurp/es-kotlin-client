package io.inbot.eskotlinwrapper

import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.File
import java.nio.charset.StandardCharsets

data class SourceFile(
    val name: String,
    val extension: String,
    val directory: String,
    val size: Long,
    val content: String
)

fun main(args: Array<String>) {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
    val esClient = RestHighLevelClient(restClientBuilder)
    val dao = esClient.crudDao<SourceFile>("demo")

    reindex(esClient, dao)

//    listFiles(dao)

//    breakItDown(dao, "directory")
//    breakItDown(dao, "extension")
//
    findFiles(dao, "bulk")

    esClient.close()
}

fun findFiles(dao: IndexDAO<SourceFile>, term: String) {
    println("All files containing the word bulk")
    println(dao.search {
        source(SearchSourceBuilder.searchSource().query(MatchQueryBuilder("content", term)))
    }.searchResponse.stringify(true))
}

fun breakItDown(dao: IndexDAO<SourceFile>, field: String) {
    val results = dao.search {
        source(
"""
{
    "size": 0,
    "aggs": {
        "by_directory": {
            "terms": {
                "field":"$field"
            }
        }
    }
}
""".trimIndent()
        )
    }

    println(
"""
Top $field's:

${results.searchResponse.stringify(true)}

""".trimIndent()
    )
}

fun reindex(
    esClient: RestHighLevelClient,
    dao: IndexDAO<SourceFile>
) {
    try {
        esClient.indices().delete(DeleteIndexRequest("demo"))
    } catch (e: Exception) {
        println("so it did not exist ...")
    }
    esClient.indices().create(
        CreateIndexRequest("demo").mapping(
            "demo", """
{
    "demo": {
        "properties": {
            "name": {
                "type":"keyword"
            },
            "extension": {
                "type":"keyword"
            },
            "directory": {
                "type":"keyword"
            }
        }
    }
}

""".trimIndent(), XContentType.JSON
        )
    )

    val allowedExtensions = setOf("java", "kt", "py", "sh")

    dao.bulk(refreshPolicy = WriteRequest.RefreshPolicy.NONE, bulkSize = 300) {
        File("/Users/jillesvangurp/git").walk().forEach {
            if (it.isFile && allowedExtensions.contains(it.extension)) {
                println("indexing ${it.name}")
                index(
                    it.name,
                    SourceFile(
                        it.name,
                        it.extension,
                        it.parentFile.canonicalPath,
                        it.length(),
                        it.readText(StandardCharsets.UTF_8)
                    ),
                    create = false
                )
            }
        }
    }
    println("done!\n\n")
}

fun listFiles(dao: IndexDAO<SourceFile>) {
    println("a list of stuff we indexed\n")
    dao.search(scrolling = true, scrollTtlInMinutes = 1) { }.mappedHits.forEach {
        println("${it.directory} - ${it.name}: ${it.size}")
    }
}