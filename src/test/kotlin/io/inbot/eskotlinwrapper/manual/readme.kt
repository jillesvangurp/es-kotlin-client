package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.dsl.MatchQuery
import io.inbot.eskotlinwrapper.dsl.TermQuery
import io.inbot.eskotlinwrapper.dsl.bool
import io.inbot.eskotlinwrapper.withTestIndex
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.elasticsearch.action.search.dsl
import org.elasticsearch.client.asyncIndexRepository
import org.elasticsearch.client.configure
import org.elasticsearch.client.indexRepository

val readme by withTestIndex<Thing, Lazy<String>>(index = "manual", refreshAllowed = true, createIndex = false) {
    sourceGitRepository.md {
        +"""
            [![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)
            [![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

            Elastic's `HighLevelRestClient` is written in Java and provides access to essentially everything in the 
            REST API that Elasticsearch exposes. It's a very powerful Java API
            but maybe not the easiest thing to work with directly. 
            
            **The kotlin client takes away none of that but adds a lot of power and convenience.**
            
            - Extensible **Kotlin DSLs for Querying, Mappings, Bulk Indexing, and Object manipulation**. These provide type safe support for commonly used things such as match and bool queries. At this point most commonly used queries are supported including all full-text queries, compound queries, and term-level queries.
            - Things that are not supported, are easy to configure via Kotlin's Map DSL or by using indexed map properties. Additionally, it is easy to extend the query dsl with your own constructions (pull requests welcome) if you are using some query or mapping construction that is not yet supported.  
            - Kotlin Extension functions, default argument values, delegate properties, and many other **kotlin features** add convenience and get rid of 
            Java specific boilerplate. The Java client is designed for Java users and comes with a lot of things that are a bit awkward / non idiomatic in Kotlin. This client cuts down on the boiler plate and uses Kotlin's DSL features, extension functions, etc. to layer a 
            friendly API over the underlying client functionality.
            - A **repository** abstraction that allows you to: 
                - Manage indices with a flexible DSL for mappings.
                - Serialize/deserialize JSON objects using your favorite serialization framework. A Jackson implementation comes with the client but you can trivially add support for other frameworks. Deserialization support is also available on search results and the bulk API.
                - Do CRUD on json documents with safe updates that retry in case of a version conflict.
                - Bulk indexing DSL to do bulk operations without boiler plate and with fine-grained error handling (via callbacks)
                - Search & count objects in the index using a Kotlin Query DSL or simply use raw json from either a file or a templated multiline kotlin string. Or if you really want, you can use the Java builders that come with the RestHighLevelClient.
                - Much more
                - **Co-routine friendly** & ready for reactive usage. We use generated extension functions that we add with a source generation plugin to add cancellable suspend functions for almost all client functions. Additionally, the before mentioned `IndexRepository` has an `AsyncIndexRepository` variant with suspending variants of the same functionality. Where appropriate, Kotlin's new `Flow` API is used.
                - This means this Kotlin library is currently the most convenient way to use Elasticsearch from e.g. Ktor or Spring Boot if you want to use 
                asynchronous IO. Using the Java client like this library does is of course possible but will end up being very boiler plate heavy.
            
            ## Documentation
            
            - [manual](https://www.jillesvangurp.com/es-kotlin-manual/) A growing collection of executable examples. This manual is 
            generated from kotlin code and all the examples in it are run as part of the test suite. This is the best
            place to get started.
            - The same manual as an **[epub](book.epub)**. Very much a work in progress. Please give me feedback on this. I may end up self publishing this at some point.
            - [dokka api docs](https://htmlpreview.github.io/?https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/docs/es-kotlin-wrapper-client/index.html) - API documentation - this gets regenerated for each release and should usually be up to date. But you can always `gradle dokka` yourself.
            - Some stand alone examples are included in the examples source folder.
            - The tests test most of the important features and should be fairly readable and provide a good overview of
             how to use things.
            - [Elasticsearch java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this kotlin wrapper does is add stuff. Elasticsearch has awesome documentation for this.
            - [demo project](https://github.com/jillesvangurp/es-kotlin-demo) - Note, this is outdated and has largely been replaced by the manual mentioned above.
            
            ## Example
            
            This example is a bit more complicated than a typical hello world but more instructive 
            than just putting some objects in a schema less index. Which of course is something you should not do. The idea here
            is to touch on most topics a software engineer would need to deal with when creating a new project using Elasticsearch:
            
            - figuring out how to create an index and define a mapping
            - populating the index with content using the bulk API
            - querying data 
        """
        snippetBlockFromClass(Thing::class, "thing-class")

        block {
            // create a Repository
            // with the default jackson model reader and writer
            // (you can use something else)
            val thingRepository = esClient.indexRepository<Thing>(
                index = "things",
                // you have to opt in to refreshes, bad idea to refresh in production code
                refreshAllowed = true
            )

            // let the Repository create the index with the specified mappings & settings
            thingRepository.createIndex {
                configure {
                    // we use our settings DSL here
                    // you can also use multi line strings with JSON in a source block
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
                    // the mapping DSL is a bit more fully featured
                    mappings {
                        // mappings DSL, most common field types are supported
                        text("name") {
                            fields {
                                // lets also add name.raw field
                                keyword("raw")
                                // and a name.autocomplete field for auto complete
                                text("autocomplete") {
                                    analyzer = "autocomplete"
                                    searchAnalyzer = "autocomplete_search"
                                }
                            }
                        }
                        // floats, longs, doubles, etc. should just work
                        number<Int>("amount")
                    }
                }
            }

            // lets put some things in our new index
            thingRepository.index("1", Thing("foo", 42))
            thingRepository.index("2", Thing("bar", 42))
            thingRepository.index("3", Thing("foobar", 42))

            // make sure ES commits the changes so we can search
            thingRepository.refresh()

            // putting things into an index 1 by 1 is not scalable
            // lets do some bulk inserts with the Bulk DSL
            thingRepository.bulk {
                // we are passed a BulkIndexingSession<Thing> in the block as 'this'

                // we will bulk re-index the objects we already added with
                // a scrolling search. Scrolling searches work just
                // like normal searches (except they are not ranked)
                // all you do is set scrolling to true and you can
                // scroll through billions of results.
                thingRepository.search(scrolling = true) {
                    // A simple example of using the Kotlin Query DSL
                    // you can also use a source block with multi line JSON
                    dsl {
                        from = 0
                        // when scrolling, this is the scroll page size
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
                    // we get a lazy sequence with deserialized Thing objects back
                    // because it's a scrolling search, we fetch pages with results
                    // as you consume the sequence.
                    if (deserialized != null) {
                        // de-serialized may be null if we disable source on the mapping
                        // uses the BulkIndexingSession to add a transformed version
                        // of the original thing
                        index(
                            esResult.id, deserialized.copy(amount = deserialized.amount + 1),
                            // to allow updates of existing things
                            create = false
                        )
                    }
                }
            }
        }
        +"""
            ## Co-routines
            
            Using co-routines is easy in Kotlin. Mostly things work the same way. Except everything is non blocking
            and asynchronous. In other languages this creates all sorts of complications that Kotlin largely avoids.
            
            The Java client in Elasticsearch has support for non blocking IO. We leverage this to add our own suspending
            calls using extension functions. Mostly these have very similar signatures as their blocking variants. But 
            of course we also added a suspending version of the Index Repository. 
        """

        block {
            // we reuse the index we created already
            val repo = esClient.asyncIndexRepository<Thing>(
                index = "things",
                refreshAllowed = true
            )
            // co routines require a CoroutineScope, so let create one
            runBlocking {
                repo.bulk {
                    // create some more things
                    (1..1000).forEach {
                        index(
                            "$it",
                            Thing("thing #$it", it.toLong())
                        )
                    }
                }
                repo.refresh()
                // if you are wondering, yes this is almost identical
                // as the synchronous version above.
                repo.search {
                    dsl {
                        query = TermQuery("name.keyword", "thing #666")
                    }
                }.mappedHits.collect {
                    // collect is part of the kotlin Flow API
                    // this is one of the few parts where the API is different
                    println("we've found an evil thing with: ${it.amount}")
                }
            }
        }

        +"""    
            For more examples, check the manual or the examples source folder.
            
            ## Code generation
            
            This library makes use of code generated by a 
            [code generation gradle plugin](https://github.com/jillesvangurp/es-kotlin-codegen-plugin). This mainly 
            used to generate co-routine friendly suspendable extension functions for all asynchronous methods in the 
            RestHighLevelClient.
            
            ## Platform support
            
            This client requires Java 8 or higher (same JVM requirements as Elasticsearch). Some of the Kotlin functionality 
            is also usable by Java developers (with some restrictions). However, you will probably want to use this from Kotlin.
            Android is currently not supported as the minimum requirements for Elastic's highlevel client are Java 8. Besides, embedding
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
        
            - I'm planning to combine the 1.0 release with an epub version of the manual that I am currently 
            considering to self publish. The idea with this is that I want the library and manual to cover all of what 
            I consider the core use cases for someone building search functionality with Elasticsearch. 
            - There are still a few missing features that I want to work on mainly related to index management and 
            the query DSL.
            - My time is limited. I work on this in my spare time and when I feel like it.
            
            If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand 
            before committing a lot of your time. I'm just inventing this as I go. Let me know what you think.
            
            ## Compatibility
            
            The general goal is to keep this client in sync with the current stable version of Elasticsearch. 
            We rely on the most recent 7.x version and only test with that.
        
            From experience, this mostly works fine against any 6.x and 7.x cluster with the exception of some changes in 
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
