# Elasticsearch Kotlin Client 

[![Download](https://api.bintray.com/packages/jillesvangurp/es-kotlin-client/es-kotlin-client/images/download.svg) ](https://bintray.com/jillesvangurp/es-kotlin-client/es-kotlin-client/_latestVersion)
[![Actions Status](https://github.com/jillesvangurp/es-kotlin-wrapper-client/workflows/CI-gradle-build/badge.svg)](https://github.com/jillesvangurp/es-kotlin-wrapper-client/actions)

The Es Kotlin Client provides a friendly Kotlin API on top of the official Elastic Java client.
Elastic's [`HighLevelRestClient`](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/master/java-rest-high.html) is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. It's a very powerful Java API that provides access to all of the oss and x-pack features. But it is not the easiest thing to work with directly. 

**The Es Kotlin Client takes away none of that but adds a lot of power and convenience.** The underlying java functionality is always there ready to be used if you need it. However, for most commonly used things, this client provides more Kotlin appropriate ways to access the functionality.

- Extensible **Kotlin DSLs for Querying, Mappings, Bulk Indexing, and Object manipulation**. These provide type safe support for commonly used things such as match and bool queries as well as defining mappings and settings for your indices. At this point most commonly used queries are supported including all full-text queries, compound queries, and term-level queries.
    - Things that are not supported are easy to configure by modifying the Map directly. For this our DSL classes delegate to a `MapBackedProperties` class that facilitates this. Of course, it is easy to extend the query dsl with your own constructions (pull requests welcome) if you are using some query or mapping construction that is not yet supported.  
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

## Get it

```
implementation("com.github.jillesvangurp:es-kotlin-client:v1.0.0-RC0-7.9.3")
```

The post-fix in the version tag indicates which version of the java client was used. As the client uses the REST API; things should generally be forward and backward compatible with Elasticsearch except of course when Elastic changes their API. 

Note, the repository was recently renamed from es-kotlin-wrapper-client to es-kotlin-client. Until recently, I also deployed versions
on jitpack. These are still available of course and if not, you can 
[regenerate them there](https://jitpack.io/#jillesvangurp/es-kotlin-client) if you need older versions.

As of RC0 I deploy via jcenter. So check there for the latest version.

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
// (you can use something else)
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
  dsl {
    // added names to the args for clarity here, but optional of course
    query = MatchQuery(field = "name", query = "nar")
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

Using co-routines is easy in Kotlin. Mostly things work the same way. Except everything is non blocking
and asynchronous. In other languages this creates all sorts of complications that Kotlin largely avoids.

The Java client in Elasticsearch has support for non blocking IO. We leverage this to add our own suspending
calls using extension functions. Mostly these have very similar signatures as their blocking variants. But 
of course we also added a suspending version of the Index Repository. 

```kotlin
// we reuse the index we created already
val repo = esClient.asyncIndexRepository<Thing>(
  index = "things",
  refreshAllowed = true
)
// co routines require a CoroutineScope, so let create one
runBlocking {
  // lets create some more things
  repo.bulk {
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
  // as the synchronous version above.
  val results = repo.search {
    dsl {
      query = TermQuery("name.keyword", "thing #666")
    }
  }
  // However, results is a Flow instead of a Sequence
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

For more examples, check the manual or the examples source folder.

## Code generation

This library makes use of code generated by a 
[code generation gradle plugin](https://github.com/jillesvangurp/es-kotlin-codegen-plugin). This mainly 
used to generate co-routine friendly suspendable extension functions for all asynchronous methods in the 
RestHighLevelClient. We may add more features in the future.

## Platform support & compatibility

This client requires Java 8 or higher (same JVM requirements as Elasticsearch). Some of the Kotlin functionality 
is also usable by Java developers (with some restrictions). However, you will probably want to use this from Kotlin.
Android is currently not supported as the minimum requirements for Elastic's highlevel client are Java 8. Besides, embedding
a fat library like that on Android is probably a bad idea and you should probably not be talking to Elasticsearch 
directly from a mobile phone in any case.

This library is tested with Elasticsearch 7.x. Usually we update to the latest version within days after 
Elasticsearch releases. Barring REST API changes, most of this client should work with recent 7.x releases, 
any future releases and probably also 6.x.

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
release 1.0, refactoring & api changes can still happen occasionally. Please check the issue tracker for progress on this.
 
If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand 
before committing a lot of your time. I'm just inventing this as I go. Let me know what you think.

## License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. 
Please exercise your rights under this license in any way you feel is appropriate. Forking is allowed and encouraged. 
I do appreciate attribution and pull requests ...

## Support

I'm available for consultancy and specialize in a few things, including Elasticsearch. I've supported small
and large companies with search related product features, complex migrations, query optimizations, plugin
development, and more. I have over 15 years of experience with Lucene, Solr, and of course Elasticsearch.


