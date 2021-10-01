[![](https://jitpack.io/v/jillesvangurp/es-kotlin-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-client)
[![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

The Es Kotlin Client provides a friendly Kotlin API on top of the official Elastic Java client.
Elastic's [`HighLevelRestClient`](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html) is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. This API provides access to the oss and x-pack features. Unfortunately, the Java client is not the easiest thing to work with directly and it also poorly matches idiomatic Kotlin.

**The Es Kotlin Client takes away none of that power but adds a lot of power and convenience.**

The underlying java functionality is always there should you need it. But, for most commonly used things, this client provides more Kotlin appropriate ways to access the functionality.

## Features

- Extensible **Kotlin DSLs for Search, MultiSearch, Mappings, Bulk Indexing, and Object CRUD**. These Kotlin Domain Specific Languages (DSL) provide type safe support for commonly used things such as `match` and `bool` queries, easy boiler-plate free bulk indexing with error handling, and creating mappings and index settings. At this point we support most commonly used queries; including all full-text queries, compound queries, and term-level queries.
  - Things that are not explicitly supported are easy to configure by modifying the underlying data model directly using Kotlin's syntactic sugar for working with `Map`.
  - You can also extend the DSL via the `MapBackedProperties` class that backs normal type safe kotlin properties with a `Map` in our DSL. So, anything that's not supported, you can just add yourself. Pull requests are welcome! To get started with this, look at the source code for the existing DSL.  
- Kotlin Extension functions, default argument values, delegate properties, and many other **kotlin features** add lots convenience and gets rid of all the Java specific boilerplate.
- A **repository** abstraction that allows you to represent an Index with a data class: 
  - Manage indices with a flexible DSL for mappings.
  - Serialize/deserialize JSON objects using your favorite serialization framework. A Jackson implementation comes with the client but you can trivially add support for other frameworks. Deserialization support is also available on search results and the bulk API.
  - Do CRUD on json documents with safe updates that retry in case of a version conflict.
  - Bulk indexing DSL to do bulk operations without boiler plate and with fine-grained error handling (via callbacks)
  - Search & count objects in the index using a Kotlin Query DSL or simply use raw json from either a file or a templated multiline kotlin string. Or if you really want, you can use the Java builders that come with the RestHighLevelClient.
  - Much more
- **Co-routine friendly** & ready for reactive usage. We use generated extension functions that we add with a source generation plugin to add cancellable suspend functions for almost all client functions. Additionally, the before mentioned `IndexRepository` has an `AsyncIndexRepository` variant with suspending variants of the same functionality. Where appropriate, we use Kotlin's `Flow` API.
- This means this Kotlin library is currently the most convenient way to use Elasticsearch from e.g. Ktor, Quarkus or Spring Boot if you want to use asynchronous IO. Using the Java client like this library does is of course possible but will end up being very boiler-plate heavy. Additionally, you'll be dealing with e.g. Spring's flux way of doing asynchronous computing.

## Get It

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-client)

As JFrog is shutting down JCenter, the latest releases are once more available via Jitpack. Add this to your `build.gradke.kts`:

```kotlin
implementation("com.github.jillesvangurp:es-kotlin-client:<version>")
```

You will also need to add the Jitpack repository:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

See [release notes](https://github.com/jillesvangurp/es-kotlin-wrapper-client/releases) with each github release tag for an overview what changed.

## Elasticsearch Java client version

This library is always built with and tested against specific versions of the Elasticsearch Java client (see release notes). Since they sometimes change their Java internal APIs, even between minor versions, it is important to match the version you are using with what the es-kotlin-client was built against. Especially with frameworks like Spring, you may end up with older versions of the java client on your classpath. 

If you see class not found or method not found related exceptions, that is probably what is happening. If, so, double check your dependencies and transitive dependencies and add excludes. Also, be careful using e.g. the spring-dependency-management plugin for this reason.

## Documentation

- [manual](https://www.jillesvangurp.com/es-kotlin-manual/) The manual for this client. I created this using my [kotlin4example](https://github.com/jillesvangurp/kotlin4example) library. So, all the examples in the manual (and the README) are working and correct. The manual covers everything from getting started, doing bulk indexing, working with co-routines, and of course doing searches.
- The same manual as an **[epub](https://www.jillesvangurp.com/es-kotlin-manual/book.epub)**. Very much a work in progress. Please give me feedback on this. I may end up self publishing this at some point.
- [dokka api docs](https://www.jillesvangurp.com/es-kotlin-manual/docs/es-kotlin-wrapper-client/index.html) - API documentation - this gets regenerated for each release and should usually be up to date. But you can always `gradle dokka` yourself.
- Some stand alone examples are included in the examples source folder. This currently includes a small ktor project that implements a recipe search engine.
- The unit and integrations tests cover most of the important features and should be fairly readable and provide a good overview of how to use things.
- [Elasticsearch java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this kotlin wrapper does is add stuff. Elasticsearch has awesome documentation for their client.