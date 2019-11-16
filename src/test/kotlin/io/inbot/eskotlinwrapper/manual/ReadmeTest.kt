package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test

// TODO clean this up
val md = """
[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)
[![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is a client library written in Kotlin that adapts the official [Highlevel Elasticsearch HTTP client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) (introduced with Elasticsearch 6.x) with some Kotlin specific goodness. 

The highlevel elasticsearch client is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. The kotlin wrapper takes away none of that and adds some power and convenience to it.

## Why?

The Java client is designed for Java users and comes with a lot of things that are a bit awkward in Kotlin. This client cuts down on the boiler plate and uses Kotlin's DSL features, extension functions, etc. to layer a friendly API over the underlying client functionality. 

Kotlin also has support for co-routines and we use this to make using the asynchronous methods in the Java client a lot nicer to use. Basics for this are in place but Kotlin's co-routine support is still evolving and some of the things we use are still labeled experimental.

## Code generation

As of 1.0-M1, this library makes use of code generated using my 
[code generation gradle plugin](https://github.com/jillesvangurp/es-kotlin-codegen-plugin) for gradle. This uses 
reflection to generate extension functions for a lot of the Java SDK. E.g. all asynchronous functions gain a 
co-routine friendly variant this way. Future versions of this plugin will add more Kotlin specific stuff. 
One obvious thing I'm considering is generating kotlin DSL extension functions for the builders in the java sdk. 
Builders are a Java thing and this would be a nice thing to have. 

Ideas welcome ...

## Platform support

This client requires Java 8 or higher (same JVM requirements as Elasticsearch). Some of the Kotlin functionality is also usable by Java developers (with some restrictions). However, you will probably want to use this from Kotlin. Android is currently not supported as the minimum requirements for the highlevel client are Java 8. Besides, embedding a fat library like that on Android is probably a bad idea and you should probably not be talking to Elasticsearch directly from a mobile phone in any case.

# Documentation

- [manual](manual/index.md) I have a growing collection of executable examples. This manual is 
actually generated using kotlin code and all the examples are actually run as part of the test suite. This is the best
place to get started.
- The tests test most of the important features and should be fairly readable and provide a good overview of
 how to use things. I like keeping the tests somewhat readable.
- [dokka api docs](https://htmlpreview.github.io/?https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/docs/es-kotlin-wrapper-client/index.html) - API documentation - this gets regenerated for each release and should usually be up to date. But you can always `gradle dokka` yourself.
- [Elasticsearch java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this kotlin wrapper does is add stuff. Elasticsearch has awesome documentation for this.
- [demo project](https://github.com/jillesvangurp/es-kotlin-demo) - Note, this is outdated and I'm replacing it with a manual in this project.

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

# A few notes on Co-routines

Note, co-routines are a **work in progress** and things may change as Kotlin evolves and as my understanding evolves. 
This is all relatively new in Kotlin and there is still a lot of stuff happening around e.g. the 
`Flow` and `Channel` concepts.

The `RestHighLevelClient` exposes asynchronous variants of all API calls as an alternative to the synchronous ones. 
The main difference is that these use the asynchronous http client instead of the synchronous one. Additionally 
they use callbacks to provide a response or an error. We provide a `SuspendingActionListener` that adapts this to 
Kotlin's co-routines.

For example, here is the async search [extension function](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/main/kotlin/org/elasticsearch/client/KotlinExtensions.kt) we add to the client:
```
suspend fun RestHighLevelClient.searchAsync(
    requestOptions: RequestOptions = RequestOptions.DEFAULT,
    block: SearchRequest.() -> Unit
): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return suspending {
        this.searchAsync(searchRequest, requestOptions, it)
    }
}
```

The `suspending` call here creates a [`SuspendingActionListener`](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/main/kotlin/io/inbot/eskotlinwrapper/SuspendingActionListener.kt) 
and passes it as `it` to the lambda. Inside the lambda we simply pass that into the searchAsync call. There are more 
than a hundred async methods in the RestHighLevel client and we currently don't cover most of them but you can easily 
adapt them yourself by doing something similar.

# Building

You need java >= 8 and docker + docker compose (to run elasticsearch and the tests).

Simply use the gradle wrapper to build things:

```
./gradlew build
```
Look inside the build file for comments and documentation.

If you create a pull request, please also regenerate the documentation with gradle dokka. We host the [API documentation](docs) inside the repo as github flavored markdown.

Gradle will spin up Elasticsearch using the docker compose plugin for gradle and then run the tests against that. If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster and making a mess there (I learned that lesson a long time ago).

# Development status

This library should be perfectly fine for general use at this point. Also note, that you can always access the underlying Java client, which is stable. However, until we release 1.0, refactoring can still happen ocassionally.

Currently the main blockers for a 1.0 are:

- ES 7.5.x should include my pull request to enable suspendCancellableCoRoutine
- We recently added reflection based code generation that scans the sdk and adds some useful extension functions. More feature work here is coming.
- Currently asynchronous is optional and I want to make this the default way of using the library as this can be so nice with co-routines and should be the default in a modern web server. 

If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand before committing a lot of your time. I'm just inventing this as I go. Let me know what you think.

## Compatibility

The general goal is to keep this client in sync with the current stable version of Elasticsearch. We rely on the most recent 7.x version. From experience, this mostly works fine against any 6.x node with the exception of some changes in APIs or query DSL; and possibly some older versions. Likewise, forward compatibility is generally not a big deal barring major changes such as the removal of types in v7.

For version 6.x, you can use the es-6.7.x branch or use one of the older releases (<= 0.9.11). Obviously this lacks a lot of the recent feature work.


# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is appropriate. Forking is allowed and encouraged. I do appreciate attribution and pull requests ...

"""

class ReadmeTest {

    @Test
    fun `generate readme`() {
        KotlinForExample.markdownPage(readmePage) {
            +md
        }
    }
}
