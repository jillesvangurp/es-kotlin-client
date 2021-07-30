[![](https://jitpack.io/v/jillesvangurp/es-kotlin-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-client)
[![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

The Es Kotlin Client provides a friendly Kotlin API on top of the official Elastic Java client.
Elastic's [`HighLevelRestClient`](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html) is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. This API provides access to all of the oss and x-pack features. However, it is not the easiest thing to work with directly. 

**The Es Kotlin Client takes away none of that power but adds a lot of power and convenience.** 

The underlying java functionality is always there ready to be used if you need it. However, for most commonly used things, this client provides more Kotlin appropriate ways to access the functionality.

## Features

- Extensible **Kotlin DSLs for Querying, Mappings, Bulk Indexing, and Object CRUD**. These kotlin DSLs provide type safe support for commonly used things such as match and bool queries as well as defining mappings and settings for your indices. At this point most commonly used queries are supported including all full-text queries, compound queries, and term-level queries.
    - Things that are not supported are easy to configure by modifying the underlying data model directly using Kotlin's syntactic sugar for working with `Map`. 
    - To enable this our DSL classes delegate to a `MapBackedProperties` class that backs normal type safe kotlin properties with a `Map`. Anything that's not supported, you can just add yourself. Additionally, it is easy to extend the DSLs with your own type safe constructions (pull requests welcome) if you are using some query or mapping construction that is not yet supported.  
- Kotlin Extension functions, default argument values, delegate properties, and many other **kotlin features** add convenience and get rid of 
Java specific boilerplate. The Java client is designed for Java users and comes with a lot of things that are a bit awkward / non idiomatic in Kotlin. This client cuts down on the boiler plate and uses Kotlin's DSL features, extension functions, etc. to layer a 
friendly API over the underlying client functionality.
- A **repository** abstraction that allows you represent an Index with a data class: 
    - Manage indices with a flexible DSL for mappings.
    - Serialize/deserialize JSON objects using your favorite serialization framework. A Jackson implementation comes with the client but you can trivially add support for other frameworks. Deserialization support is also available on search results and the bulk API.
    - Do CRUD on json documents with safe updates that retry in case of a version conflict.
    - Bulk indexing DSL to do bulk operations without boiler plate and with fine-grained error handling (via callbacks)
    - Search & count objects in the index using a Kotlin Query DSL or simply use raw json from either a file or a templated multiline kotlin string. Or if you really want, you can use the Java builders that come with the RestHighLevelClient.
    - Much more
- **Co-routine friendly** & ready for reactive usage. We use generated extension functions that we add with a source generation plugin to add cancellable suspend functions for almost all client functions. Additionally, the before mentioned `IndexRepository` has an `AsyncIndexRepository` variant with suspending variants of the same functionality. Where appropriate, Kotlin's new `Flow` API is used.
- This means this Kotlin library is currently the most convenient way to use Elasticsearch from e.g. Ktor or Spring Boot if you want to use 
 asynchronous IO. Using the Java client like this library does is of course possible but will end up being very boiler plate heavy.

## Get it

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-client)

As JFrog is shutting down JCenter, the latest releases are once more available via Jitpack. 
Add this to your `build.gradke.kts`            

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

See [release notes](https://github.com/jillesvangurp/es-kotlin-wrapper-client/releases) with each github release 
tag for an overview what changed.
            
## Documentation & Support

- [manual](https://www.jillesvangurp.com/es-kotlin-manual/) A growing collection of executable examples. This manual is 
generated from kotlin code and all the examples in it are run as part of the test suite. This is the best
place to get started.
- The same manual as an **[epub](https://www.jillesvangurp.com/es-kotlin-manual/book.epub)**. Very much a work in progress. Please give me feedback on this. I may end up self publishing this at some point.
- [dokka api docs](https://www.jillesvangurp.com/es-kotlin-manual/docs/es-kotlin-wrapper-client/index.html) - API documentation - this gets regenerated for each release and should usually be up to date. But you can always `gradle dokka` yourself.
- Some stand alone examples are included in the examples source folder.
- The tests test most of the important features and should be fairly readable and provide a good overview of
 how to use things.
- [Elasticsearch java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this kotlin wrapper does is add stuff. Elasticsearch has awesome documentation for this.
- [demo project](https://github.com/jillesvangurp/es-kotlin-demo) - Note, this is outdated and has largely been replaced by the manual mentioned above.
- Within reason, I'm always happy to **support** users with issues. Feel free to approach me via the issue tracker, twitter (jillesvangurp), or email (jilles AT jillesvangurp.com). I'm also available for consulting on Elasticsearch projects and am happy to help you with architecture reviews, implementation work, query & indexing strategy, etc. Check my [homepage for more details](https://jillesvangurp.com).
