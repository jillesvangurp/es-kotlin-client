package io.inbot.eskotlinwrapper

import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
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
    val line: String
)

val demoMapping = """
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

""".trimIndent()

fun main(args: Array<String>) {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
    val esClient = RestHighLevelClient(restClientBuilder)
    val dao = esClient.crudDao<SourceFile>("demo")

//    reindex(esClient, dao)

//    listFiles(dao)

    breakItDown(dao, "name")
//    breakItDown(dao, "extension")
//    breakItDown(dao, "directory")

//    searchLines(dao, "kotlin")
//    searchLines(dao, "wtf")
//    searchLines(dao, "shit")

    esClient.close()
}

fun reindex(
    esClient: RestHighLevelClient,
    dao: IndexDAO<SourceFile>
) {
    try {
        esClient.indices().delete(DeleteIndexRequest("demo"), RequestOptions.DEFAULT)
    } catch (e: Exception) {
        println("index did not exist, moving on ...")
    }

    esClient.indices().create(
        CreateIndexRequest("demo").mapping(
            "demo", demoMapping, XContentType.JSON
        ), RequestOptions.DEFAULT
    )

    val allowedExtensions = setOf("java", "kt", "sh")

    dao.bulk(refreshPolicy = WriteRequest.RefreshPolicy.NONE, bulkSize = 1000) {
        File("/Users/jillesvangurp/git").walk().forEach { file ->
            if (file.isFile && allowedExtensions.contains(file.extension)) {
                println("indexing ${file.name}")

                var lineCounter = 0
                file.readLines(StandardCharsets.UTF_8).forEach { line ->
                    val id = file.name + "#" + lineCounter++
                    if (line.trim().length>2) {
                        index(
                            id,
                            SourceFile(
                                file.name,
                                file.extension,
                                file.parentFile.canonicalPath,
                                file.length(),
                                line
                            ),
                            create = false
                        )
                    }
                }
            }
        }
    }
    println("done!")
}

fun searchLines(dao: IndexDAO<SourceFile>, term: String) {
    println("All files containing the word bulk")
    println(dao.search {
        source(SearchSourceBuilder.searchSource().query(MatchQueryBuilder("line", term)))
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

fun listFiles(dao: IndexDAO<SourceFile>) {
    println("a list of stuff we indexed\n")
    dao.search(scrolling = true, scrollTtlInMinutes = 1) { }.mappedHits.forEach {
        println("${it.directory} - ${it.name}: ${it.size}")
    }
}