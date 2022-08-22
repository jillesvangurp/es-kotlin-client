# Elasticsearch Kotlin Client 

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-client)
[![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

# **This project is now deprecated!**

**es-kotlin-client** becomes [kt-search](https://github.com/jillesvangurp/kt-search)

The 2.0 version of this client lives in [a new repository](https://github.com/jillesvangurp/kt-search). The name `kt-search` better reflects that this client works with both Opensearch 1 & 2 and Elasticsearch 7 & 8. Additionally, changes in the ecosystem (Opensearch fork, compatibility with os/es clients, deprecation of RestHighLevelClient) necessitated some major internal changes. 

**As of now, es-kotlin-client is deprecated and no longer maintained.** However, it lives on as the `legacy-client` module in `kt-search`. When we release kt-search, using that will be your upgrade path.

**Why?** Elasticsearch was forked by Amazon as Opensearch and there are now two products that implement almost identical REST APIs that require separate Java clients. Additionally, Elastic deprecated their client and wrote a completely new one. And finally they also released a major new version with many changes. As of now, es-kotlin-client is only compatible with Elasticsearch v7. The Elastic RestHighLevel client does not work with Opensearch anymore and the Opensearch fork of this client has renamed packages. And because it never was very usable and now deprecated on the Elastic side, an extensive rewrite of this library could not be avoided.


Relative to es-kotlin-client, kt-search is a complete rewrite that preserves the best features:

- friendly DSLs for search, bulk indexing, mappings, etc.
- kotlin friendly
- repository abstraction 
- asynchronous IO with co-routines

Additionally:

- no more dependencies on a Java client
- kotlin multi platform. This will work on jvm, ios, android, and the web.
- support for Elasticsearch 7 & 8 and Opensearch
- designed to be extensible

Development work is ongoing but the client is already usable. I'll update this page with more info once a 2.0 stabilizes and we can freeze the APIs.

**16 May 2022**

I'm working on a version 2.0 of this library. A lot of things happened recently:

- The Opensearch fork happened last year. Like everyone, I'm dealing with people that use that instead of Elasticsearch. So, I have a need to be able to cover both with 1 library.
- Elasticsearch 8 was released some time ago. It's largely backwards compatible. But it's a major version nonetheless and of course all of the new stuff is Elasticsearch only.
- The RestHighLevel client that this project depends on was 1) deprecated 2) cannot be used with OpenSearch in recent versions. The replacement library is Elasticsearch only. 
- Kotlin Multiplatform is happening. 

As a consequence, version 2.0 of this project will try to preserve the essential and popular features of this project (DSL support, co-routine support, repositories, bulk indexing, etc.) but it will rebuild them on top of a kotlin multiplatform basis.

Specifically:

- All dependencies on the Elastic client will be removed. You may still use them in your onw project and I plan to make it possible to use the Opensearch or Elasticsearch rest client as one of several ways to do http connectivity. That way if you use either of those, adding the kotlin client should be easy.
- For Json serialization and parsing, the client will continue to provide multiple options. The default will likely be kotlinx.serialization. But you should be able to continue to use jackson, gson, etc.
- Some of the Elastic specific extension functions may live on in a separate library. Depending on interest and needs of users.
- Version 2.0 will no longer need the code generation plugin we used to add asynchronous, suspend functions for the RestHighLevel client API. So that project is as of now end of life and will no longer receive any updates.
- Going forward, all APIs will be non blocking / suspend only. It makes no sense to support blocking IO with modern Kotlin.

Work for this is ongoing on the `search-client-2.0` branch. There will likely be lots of API changes. I expect this project will stabilize over the next few weeks.

Barring any emergencies, I won't provide new releases of the 1.x branch. The latest 1.x version should be fine with most recent versions of Elasticsearch. If people want to fork and fix, that's fine with me but I'm not looking to merge pull requests for 1.x related fixes. 

As soon as the 2.0 branch stabilizes, I will merge it to master. 1.x has already been branched to its own branch.

<hr>

## Version 1.x README

The Es Kotlin Client provides a friendly Kotlin API on top of the official Elastic Java client.
Elastic's [`HighLevelRestClient`](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html) is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. This API provides access to the oss and x-pack features. Unfortunately, the Java client is not the easiest thing to work with directly and it also poorly matches idiomatic Kotlin.

**The Es Kotlin Client takes away none of that power but adds a lot of power and convenience.**

The underlying java functionality is always there should you need it. But, for most commonly used things, this client provides more Kotlin appropriate ways to access the functionality.

## The new Java API Client introduced in 7.15

In Elasticsearch 7.15, a new Java API client was added to replace the Java REST High Level Client on which the kotlin library is based. Given that and the recent fork with Amazon's Opensearch, and the coming Elasticsearch 8.0 release.  

For now, the kotlin client will continue to use the deprecated java RestHighLevel client. I'm currently considering several options for the future of this Kotlin client. I was in any case considering to start work on a major release of this library to rebuild it on top of ktor client and kotlinx serialization and gradually cut loose from the Java client.

With Opensearch and Elasticsearch 8 clearly diverging in terms of API compatibility, features, and indeed compatibility with the Java client, compatibility breaking changes are inevitable. So, cutting loose from the Java client seems like it could be the right strategy and would also enable using this kotlin client on kotlin native, kotlin js, or soon kotlin WASM.

Obviously this is going to be a bit of work and I need to find time to be able to commit to this.

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

## Example

This example is a bit more complicated than a typical hello world but more instructive 
than just putting some objects in a schema less index. Which of course is something you should not do. The idea here
is to touch on most topics a software engineer would need to deal with when creating a new project using Elasticsearch:

- figuring out how to create an index and define a mapping
- populating the index with content using the bulk API
- querying data 

```kotlin
// given some model class with two fields
data class Thing(
  val name: String,
  val amount: Long = 42
)
```

```kotlin
// create a Repository
// with the default jackson model reader and writer
// (you can use something else by overriding default values of the args)
val thingRepository = esClient.indexRepository<Thing>(
  index = "things",
  // you have to opt in to refreshes, bad idea to refresh in production code
  refreshAllowed = true
)

// let the Repository create the index with the specified mappings & settings
thingRepository.createIndex {
  // we use our settings DSL here
  // you can also choose to use a source block with e.g. multiline strings
  // containing json
  configure {

    settings {
      replicas = 0
      shards = 2
    }
    mappings {
      // mappings DSL, most common field types are supported
      text("name")
      // floats, longs, doubles, etc. should just work
      number<Int>("amount")
    }
  }
}

// lets create a few Things
thingRepository.index("1", Thing("foo", 42))
thingRepository.index("2", Thing("bar", 42))
thingRepository.index("3", Thing("foobar", 42))

// make sure ES commits the changes so we can search
thingRepository.refresh()

val results = thingRepository.search {
  configure {
    // added names to the args for clarity here, but optional of course
    query = match(field = Thing::name, query = "bar")
  }
}
// results know hot deserialize Things
results.mappedHits.forEach {
  println(it.name)
}
// but you can also access the raw hits of course
results.searchHits.forEach {
  println("hit with id ${it.id} and score ${it.score}")
}

// putting things into an index 1 by 1 is not scalable
// lets do some bulk inserts with the Bulk DSL
thingRepository.bulk {
  // we are passed a BulkIndexingSession<Thing> in the block as 'this'

  // we will bulk re-index the objects we already added with
  // a scrolling search. Scrolling searches work just
  // like normal searches (except they are not ranked)
  // all you do is set scrolling to true and you can
  // scroll through billions of results.
  val sequence = thingRepository.search(scrolling = true) {
    configure {
      from = 0
      // when scrolling, this is the scroll page size
      resultSize = 10
      query = bool {
        should(
          // you can use strings
          match("name", "foo"),
          // or property references
          match(Thing::name, "bar"),
          match(Thing::name, "foobar")
        )
      }
    }
  }.hits
  // hits is a Sequence<Pair<SearchHit,Thing?>> so we get both the hit and
  // the deserialized value. Sequences are of course lazy and we fetch
  // more results as you process them.
  // Thing is nullable because Elasticsearch allows source to be
  // disabled on indices.
  sequence.forEach { (esResult, deserialized) ->
    if (deserialized != null) {
      // Use the BulkIndexingSession to index a transformed version
      // of the original thing
      index(
        esResult.id, deserialized.copy(amount = deserialized.amount + 1),
        // allow updates of existing things
        create = false
      )
    }
  }
}
```

## Co-routines

Using co-routines is easy in Kotlin. Mostly things work almost the same way. Except everything is non blocking
and asynchronous, which is nice. In other languages this creates all sorts of complications that Kotlin largely avoids.

The Java client in Elasticsearch has support for non blocking IO. We leverage this to add our own suspending
calls using extension functions via our gradle code generation plugin. This runs as part of the build process for this
 library so there should be no need for you to use  this plugin. 

The added functions have the same signatures as their blocking variants. Except of course they have the 
word async in their names and the suspend keyword in front of them. 

We added suspending versions of the `Repository` and `BulkSession` as well, so either blocking or non
blocking. It's up to you.

```kotlin
// we reuse the index we created already to create an ayncy index repo
val repo = esClient.asyncIndexRepository<Thing>(
  index = "things",
  refreshAllowed = true
)
// co routines require a CoroutineScope, so let use one
runBlocking {
  // lets create some more things; this works the same as before
  repo.bulk {
    // but we now get an AsyncBulkIndexingSession<Thing>
    (1..1000).forEach {
      index(
        "$it",
        Thing("thing #$it", it.toLong())
      )
    }
  }
  // refresh so we can search
  repo.refresh()
  // if you are wondering, yes this is almost identical
  // to the synchronous version above.
  // However, we now get an AsyncSearchResults back
  val results = repo.search {
    configure {
      query = term("name.keyword", "thing #666")
    }
  }
  // However, mappedHits is now a Flow instead of a Sequence
  results.mappedHits.collect {
    // collect is part of the kotlin Flow API
    // this is one of the few parts where the API is different
    println("we've found a thing with: ${it.amount}")
  }
}
```

Captured Output:

```
we've found a thing with: 666

```

For more examples, check the [manual](https://www.jillesvangurp.com/es-kotlin-manual/) or look in the [examples](src/examples/kotlin) source directory.

## Code Generation

This library makes use of code generated by our 
[code generation gradle plugin](https://github.com/jillesvangurp/es-kotlin-codegen-plugin). This is mainly 
used to generate co-routine friendly suspendable extension functions for all asynchronous methods in the 
RestHighLevelClient. We may add more features in the future.

## Platform Support & Compatibility

This client requires Java 8 or higher (same JVM requirements as Elasticsearch). Some of the Kotlin functionality is also usable by Java developers (with some restrictions). However, you will probably want to use this from Kotlin.
Android is currently not supported as the minimum requirements for Elastic's highlevel client are Java 8. Besides, embedding a fat library like that on Android is probably a bad idea, and you should probably not be talking to Elasticsearch directly from a mobile phone in any case.

Usually we update to the latest version within days after Elasticsearch releases. Barring REST API changes, most of this client should work with any recent 7.x releases,
any future releases and probably also 6.x.

For version 6.x, check the es-6.7.x branch.

## Building & Development

You need java >= 8 and docker + docker compose to run elasticsearch and the tests.

Simply use the gradle wrapper to build things:

```bash
./gradlew build
```

Look inside the build file for comments and documentation.

If you create a pull request, please also regenerate the documentation with `build.sh` script. It regenerates the documentation. We host and version the documentation in this repository along with the source code.

Gradle will spin up Elasticsearch using the docker compose plugin for gradle and then run the tests against that. If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster and making a mess there (I learned that lesson a long time ago).

## Development

This library should be perfectly fine for general use at this point. We released 1.0 beginning of 2021 and I provide regular updates as new versions of Elasticsearch are published. If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand before committing a lot of your time. I'm just inventing this as I go. Let me know what you think.

## License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is appropriate. I do appreciate attribution and pull requests ...

Note, as of version 7.11.0, the Elastic Rest High Level Client that this library depends on has moved to a non OSS license. Before that, it was licensed under a mix of Apache 2.0 and Elastic's proprietary licensing.

As far as I understand it, that should not change anything for using this library. Which will always be licensed under the MIT license. But if that matters to you, you may want to stick with version 1.0.12 of this library. Version 1.1.0 and onwards are going to continue to track the main line Elasticsearch.

## Support and Consulting

Within reason, I'm always happy to **support** users with issues. Feel free to approach me via the [issue tracker](https://github.com/jillesvangurp/es-kotlin-client/issues), the [discussion](https://github.com/jillesvangurp/es-kotlin-client/discussions) section, twitter (jillesvangurp), or email (jilles AT jillesvangurp.com).

I generally respond to all questions, issues, and pull requests. But, please respect that I'm doing this in my spare time.

I'm also available for consulting/advice on Elasticsearch projects and am happy to help you with architecture reviews, query & indexing strategy, etc. I can do trainings, deep dives and have a lot of experience delivering great search. Check my [homepage for more details](https://jillesvangurp.com).


