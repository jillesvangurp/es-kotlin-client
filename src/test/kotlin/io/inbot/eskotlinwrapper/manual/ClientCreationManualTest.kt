package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.create
import org.junit.jupiter.api.Test

class ClientCreationManualTest : AbstractElasticSearchTest(indexPrefix = "manual") {

    @Test
    fun `explain client creation`() {
        KotlinForExample.markdownPage(createClientPage) {
            +"""
                To use the Kotlin extension functions included with this library, you have to create an instance
                of the Java High Level Restclient. There are several ways to do this.
                                
                ## Do it the Java way 
                
                If you have used the Java Highlevel client before, there is nothing special that you need to do. 
                You simply create a Java Highlevel Rest client as usual.
            """

            block() {
                val restClientBuilder = RestClient.builder(
                    HttpHost("localhost", 9200, "http")
                )
                val restHighLevelClient = RestHighLevelClient(restClientBuilder)
            }

            +"""
                ## Use the exension function
                
                There are a lot of options you can configure on the rest high level client. To cover the 
                common use cases, this library includes a convenient extension function that you can call. 
            """
            block() {
                // connects to localhost:9200
                val restHighLevelClient = create()
            }

            +"""
                The `create` function has optional parameters that you can set. This is a pattern with Kotlin where
                we avoid using builders and instead provide sane defaults that you optionally override.
                
                For example, this is how you would connect to Elastic Cloud:
            """

            block() {
                // connects to localhost:9200
                val restHighLevelClient = create(
                    host = "416ecf12457f486db3a30bdfbd21435c.eu-central-1.aws.cloud.es.io",
                    port = 9243,
                    https = true,
                    user = "admin",
                    password = "secret" // please use something more secure
                )
            }

            +"""
                ## Set up cluster sniffing
                
                If your application has direct access to the Elasticsearch cluster and is not using a load balancer,
                you can use client side load balancing. For this purpose, the create function has a `useSniffing` 
                parameter. Obviously, this does not work if you are hosting in Elastic Cloud because in that case your
                cluster is behind a load balancer.
                
                Sniffing allows the client to discover the cluster from an initial node and allows it to do
                simple round robing load balancing as well as recover from nodes disappearing. Both are useful features 
                to have in production environment. 
            """
            block() {
                val restHighLevelClient = create(
                    host = "localhost",
                    port = 9200,
                    useSniffer = true,
                    // if requests fail, the sniffer will try to discover non failed nodes
                    sniffAfterFailureDelayMillis = 2000,
                    // regularly discover nodes in the cluster
                    sniffIntervalMillis = 30000
                )
            }
        }
    }
}
