package io.inbot.eskotlinwrapper

import org.apache.http.HttpHost
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.MatchQueryBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.File
import java.nio.charset.StandardCharsets

data class KotlinFile(val name: String, val directory: String, val size: Long, val content: String)

fun main(args: Array<String>) {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
    val esClient = RestHighLevelClient(restClientBuilder)

    esClient.indices().delete(DeleteIndexRequest("demo"))
    esClient.indices().create(CreateIndexRequest("demo").mapping("demo", """
{
    "demo": {
        "properties": {
            "name": {
                "type":"keyword"
            },
            "directory": {
                "type":"keyword"
            }
        }
    }
}

""".trimIndent(), XContentType.JSON))

    val dao = esClient.crudDao<KotlinFile>("demo")

    dao.bulk {
        File("./src").walk().forEach {
            if (it.isFile) {
                index(it.name, KotlinFile(it.name, it.parentFile.absolutePath, it.length(), it.readText(StandardCharsets.UTF_8)), create = false)
            }
        }
    }

    println("a list of stuff we indexed\n\n")
    dao.search(scrollTtlInMinutes = 1) { }.mappedHits.forEach {
        println("${it.directory} - ${it.name}: ${it.size}")
    }

    val results = dao.search {
        source(
            """
                {
                    "size": 0,
                    "aggs": {
                        "by_directory": {
                            "terms": {
                                "field":"directory"
                            }
                        }
                    }
                }
            """.trimIndent()
        )
    }

    println("""
        Top directories by file count:

        ${results.searchResponse.stringify(true)}

        """.trimIndent())

    println("All files containing the word bulk")
    println(dao.search {
        source(SearchSourceBuilder.searchSource().query(MatchQueryBuilder("content", "bulk")))
    }.searchResponse.stringify(true))
}