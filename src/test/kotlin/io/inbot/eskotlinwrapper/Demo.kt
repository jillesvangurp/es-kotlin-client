package io.inbot.eskotlinwrapper

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import java.io.File
import java.nio.charset.StandardCharsets

data class KotlinFile(val name: String, val directory: String, val size: Long, val content: String)

fun main(args: Array<String>) {
    val restClientBuilder = RestClient.builder(HttpHost("localhost", 9999, "http"))
    val esClient = RestHighLevelClient(restClientBuilder)

    val dao = esClient.crudDao<KotlinFile>("demo")

    dao.bulk {
        File("./src").walk().forEach {
            if (it.isFile) {
                index(it.name, KotlinFile(it.name, it.parentFile.absolutePath, it.length(), it.readText(StandardCharsets.UTF_8)), create = false)
            }
        }
    }

    dao.search(scrollTtlInMinutes = 1) { }.mappedHits.forEach {
        println("${it.directory} - ${it.name}: ${it.size}")
    }
}