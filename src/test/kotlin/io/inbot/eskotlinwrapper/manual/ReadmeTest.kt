package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import io.inbot.eskotlinwrapper.dsl.MatchQuery
import io.inbot.eskotlinwrapper.dsl.bool
import org.elasticsearch.action.search.dsl
import org.elasticsearch.client.configure
import org.elasticsearch.client.indexRepository
import org.junit.jupiter.api.Test

class ReadmeTest : AbstractElasticSearchTest(indexPrefix = "manual") {

    @Test
    fun `generate readme`() {
        KotlinForExample.markdownPageWithNavigation(readmePage) {
            +"""
            [![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)
            [![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

            The ES Kotlin client for the Elasticsearch Highlevel REST client is a client library written in Kotlin that 
            adapts the official [Highlevel Elasticsearch HTTP client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) with some Kotlin specific goodness, support for Kotlin DSLs, and co-routines. 
            

            ## Highlights
            
            Elastic's `HighLevelRestClient` is written in Java and provides access to essentially everything in the 
            REST API that Elasticsearch exposes. It reuses a lot of classes that Elasticsearch uses internally that used to be part
            of their embedded Java client, which literally ran an Elasticsearch node inside your application. It's a very powerful API
            but maybe not the most easy thing to work with directly. **The kotlin wrapper takes away none of that but it adds a lot of power and convenience.**

            - Extensible **Kotlin Query and Mapping DSLs**. These provide type safe support for commonly used things such as match and bool queries. Things that are not supported, are easy to configure via Kotlin's Map DSL or by using indexed map properties. Additionally it is easy to extend the query dsl with your own constructions (pull requests welcome) if you are using some query or mapping construction that is not yet supported.  
            - Extension functions, default argument values, delegate properties, and many other **kotlin features** that add convenience and get rid of 
            Java specific boilerplate. The Java client is designed for Java users and comes with a lot of things that are a bit awkward / non idiomatic in Kotlin. This client cuts down on the boiler plate and uses Kotlin's DSL features, extension functions, etc. to layer a 
            friendly API over the underlying client functionality.
            - A **repository** abstraction that allows you to: 
                - Manage indices with a flexible DSL for mappings
                - Do crud on index items with safe updates that retry in case of a version conflict
                - Bulk indexing DSL to do bulk operations without boiler plate and with fine-grained error handling (via callbacks)
                - Search & count objects in the index using a Kotlin friendly version of their query DSL or simply use raw json from either a file or a templated multiline kotlin string.
                - Much more
            - **Co-routine friendly** & ready for reactive usage. We use generated extension functions that we add with a source generation plugin to add cancellable suspend functions for almost all client functions. Additionally, the before mentioned `IndexRepository` has an `AsyncIndexRepository` variant with suspending variants of the same functionality. 
                - This means this Kotlin library is currently the most convenient way to use Elasticsearch from e.g. Ktor or Spring Boot if you want to use 
                asynchronous IO. Using the Java client like this library does is of course possible but will end up being very boiler plate heavy.
            
            ## Documentation
            
            - [manual](https://www.jillesvangurp.com/es-kotlin-manual/) A growing collection of executable examples. This manual is 
            actually generated using kotlin code and all the examples in it are actually run as part of the test suite. This is the best
            place to get started.
            - The same manual as an **[epub](book.epub)**. Very much a work in progress. Please give me feedback on this.
            - [dokka api docs](https://htmlpreview.github.io/?https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/docs/es-kotlin-wrapper-client/index.html) - API documentation - this gets regenerated for each release and should usually be up to date. But you can always `gradle dokka` yourself.
            - Some stand alone examples are included in the examples source folder.
            - The tests test most of the important features and should be fairly readable and provide a good overview of
             how to use things. I like keeping the tests somewhat readable.
            - [Elasticsearch java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this kotlin wrapper does is add stuff. Elasticsearch has awesome documentation for this.
            - [demo project](https://github.com/jillesvangurp/es-kotlin-demo) - Note, this is outdated and has largely been replaced by the manual mentioned above.
            
            ## Example
            """

            block {
                // given some model class
                data class Thing(val name: String, val amount: Long = 42)

                // create a Repository
                // use the default jackson model reader and writer (you can customize)
                // opt in to refreshes (we don't want this in production code) so we can test
                val thingRepository = esClient.indexRepository<Thing>(
                    "things", refreshAllowed = true
                )

                // let the Repository create the index
                thingRepository.createIndex {
                    // as of 1.0-Beta-3, we have a new Mapping DSL
                    configure {
                        settings {
                            replicas = 0
                            shards = 2

                            addTokenizer("autocomplete") {
                                // the DSL does not cover everything but it's a proper
                                // map so you can fall back to adding things directly
                                this["type"] = "edge_ngram"
                                this["min_gram"] = 2
                                this["max_gram"] = 10
                                this["token_chars"] = listOf("letter")
                            }
                            addAnalyzer("autocomplete") {
                                this["tokenizer"] = "autocomplete"
                                this["filter"] = listOf("lowercase")
                            }
                            addAnalyzer("autocomplete_search") {
                                this["tokenizer"] = "lowercase"
                            }
                        }
                        mappings {
                            // most common field types are supported
                            text("name") {
                                fields {
                                    text("autocomplete") {
                                        analyzer = "autocomplete"
                                        searchAnalyzer = "autocomplete_search"
                                    }
                                }
                            }
                            number<Int>("amount")
                        }
                    }
                }

                // put some things in our new index
                thingRepository.index("1", Thing("foo", 42))
                thingRepository.index("2", Thing("bar", 42))
                thingRepository.index("3", Thing("foobar", 42))

                // make sure ES commits this so we can search
                thingRepository.refresh()

                // now lets do a little bit of reindexing logic that
                // shows off scrolling searches using our DSL and bulk indexing
                thingRepository.bulk {
                    // we are passed a BulkIndexingSession<Thing> in the block

                    thingRepository.search(scrolling = true) {
                        // our new DSL; very much a work in progress
                        // you can also use a source block with multi line
                        // strings containing json
                        dsl {
                            from = 0
                            resultSize = 10
                            query = bool {
                                should(
                                    MatchQuery("name", "foo"),
                                    MatchQuery("name", "bar"),
                                    MatchQuery("name", "foobar")
                                )
                            }
                        }
                    }.hits.forEach { (esResult, deserialized) ->
                        // we get a lazy sequence that fetches results using the scroll api in es
                        // de-serialized may be null if we disable source on the mapping
                        if (deserialized != null) {
                            // use the BulkIndexingSession to index a transformed version
                            // of the original
                            index(
                                esResult.id, deserialized.copy(amount = deserialized.amount + 1),
                                create = false
                            )
                        }
                    }
                }
            }
            +"""
            
            ## Code generation
            
            This library makes use of code generated by a 
            [code generation gradle plugin](https://github.com/jillesvangurp/es-kotlin-codegen-plugin). This plugin uses 
            reflection to generate extension functions for a lot of the Java SDK. E.g. all asynchronous functions gain a 
            co-routine friendly variant this way.  
            
            Ideas/PRs welcome ...
            
            ## Platform support
            
            This client requires Java 8 or higher (same JVM requirements as Elasticsearch). Some of the Kotlin functionality 
            is also usable by Java developers (with some restrictions). However, you will probably want to use this from Kotlin.
            Android is currently not supported as the minimum requirements for the highlevel client are Java 8. Besides, embedding
            a fat library like that on Android is probably a bad idea and you should probably not be talking to Elasticsearch 
            directly from a mobile phone in any case.
            # Get it
            
            We are using jitpack for releases currently; the nice thing is all I need to do is tag the release in Git and 
            they do the rest. They have nice instructions for setting up your gradle or pom file:
            
            [![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)
            
            See [release notes](https://github.com/jillesvangurp/es-kotlin-wrapper-client/releases) with each github release 
            tag for an overview what changed.
            
            Note. this client assumes you are using this with Elasticsearch 7.x. The versions listed in our build.gradle
            and docker-compose file are what we test with. Usually we update to the latest version within days after 
            Elasticsearch releases.
            
            For version 6.x, check the es-6.7.x branch.

            ## Building & Development
            
            You need java >= 8 and docker + docker compose (to run elasticsearch and the tests).
            
            Simply use the gradle wrapper to build things:
            
            ```
            ./gradlew build
            ```
            
            Look inside the build file for comments and documentation.
            
            If you create a pull request, please also regenerate the documentation with `build.sh` script. It regenerates 
            the documentation. We host and version the documentation in this repository along with the source code.
            
            Gradle will spin up Elasticsearch using the docker compose plugin for gradle and then run the tests against that. 
            If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find 
            that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster and making 
            a mess there (I learned that lesson a long time ago).
            
            ## Development status
            
            This library should be perfectly fine for general use at this point and is currently available as a beta release. 
            
            Please note, that you can always access the underlying Java client, which is stable. However, until we 
            release 1.0, refactoring & api changes can still happen occasionally.
            
            Currently the main blockers for a 1.0 are:

            - We recently added reflection based code generation that scans the sdk and adds some useful extension functions.
            More feature work here is coming. Particularly, I want to auto generate a kotlin query dsl from the Java builders.
            - I'm planning to combine the 1.0 release with an epub version of the manual that I am currently considering to self publish. The idea with this is that I want the library and manual to cover all of what I consider the core use cases for someone building search functionality with Elasticsearch. 
            - There are still a few missing features that I want to work on mainly related to index management and the query DSL.
            - My time is limited. I work on this in my spare time and when I feel like it.
            
            If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand 
            before committing a lot of your time. I'm just inventing this as I go. Let me know what you think.
            
            ## Compatibility
            
            The general goal is to keep this client in sync with the current stable version of Elasticsearch. We rely on the most 
            recent 7.x version. From experience, this mostly works fine against any 6.x cluster with the exception of some changes in 
            APIs or query DSL; and possibly some older versions. Likewise, forward compatibility is generally not a big deal 
            barring major changes such as the removal of types in v7. The upcoming v8 release is currently not tested but should 
            be fine. Expect a release shortly after 8.0 stabilizes. With recent release tags, I've started adding the 
            version number of the Elasticsearch release it is based on.
            
            For version 6.x, you can use the es-6.7.x branch or use one of the older releases (<= 0.9.11). Obviously this lacks a 
            lot of the recent feature work. Likewise, we will create a 7x branch when v8 is released.
            
            ## License
            
            This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. 
            Please exercise your rights under this license in any way you feel is appropriate. Forking is allowed and encouraged. 
            I do appreciate attribution and pull requests ...
            """
        }
    }
}
