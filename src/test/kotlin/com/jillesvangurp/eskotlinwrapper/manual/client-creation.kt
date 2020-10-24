@file:Suppress("UNUSED_VARIABLE")

package com.jillesvangurp.eskotlinwrapper.manual

import com.jillesvangurp.eskotlinwrapper.withTestIndex
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.create

val clientCreation by withTestIndex<Thing, Lazy<String>>(index = "manual", refreshAllowed = true, createIndex = false) {
    sourceGitRepository.md {
        +"""
            To use the ES Kotlin Client, you simply have to create an instance
            of the Java High Level Restclient (and have this library on the classpath). 
            There are several ways to do this.
                            
            ## Do it the Java way 
            
            If you have used the Java Highlevel client before, there is nothing special that you need to do. 
            You simply create a Java Highlevel Rest client as usual. For example:
        """

        block() {
            val restClientBuilder = RestClient.builder(
                HttpHost("localhost", 9200, "http")
            )
            val restHighLevelClient = RestHighLevelClient(restClientBuilder)
        }

        +"""
            ## Use the extension function
            
            The Java way is a bit boilerplate heavy. So we provide an alternative in the form of a `create()`
            extension function that has a lot of parameters with sane default values.
    
        """
        block() {
            // connects to localhost:9200
            val restHighLevelClient = create()
        }

        +"""
            The `create` function has optional parameters that you can set.  For example, this is how you would 
            connect to Elastic Cloud:
        """

        block() {
            // connects to localhost:9200
            val restHighLevelClient = create(
                host = "XXXXXXXXXX.eu-central-1.aws.cloud.es.io",
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
            cluster is behind a load balancer and the client won't be able to talk to nodes in the cluster directly.
            
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
