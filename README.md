[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is a client library written in Kotlin that adapts the official [Highlevel Elasticsearch HTTP client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) (introduced with Elasticsearch 6.x) with some Kotlin specific goodness. 

The highlevel elasticsearch client is written in Java and provides access to essentially everything in the REST API that Elasticsearch exposes. The kotlin wrapper takes away none of that and adds some power and convenience to it.

It adds extension methods, cuts down on boilerplate through use of several kotlin features for creating DSLs, default arguments, sequences. etc. Some of this is also  usable by Java developers (with some restrictions). Android is not supported as the minimum requirements for the highlevel client are Java 8. 

Kotlin also has support for co-routines and the intention is to gradually support asynchronous operations through that as well. Basics for this are in place but work is ongoing.

# Documentation

... is a work in progress

- [dokka api docs](docs/es-kotlin-wrapper-client/index.md) - API documentation. 
- [java client documentation](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) - All of the functionality provided by the java client is supported. All this project does is add stuff.
- [demo project](https://github.com/jillesvangurp/es-kotlin-demo) - Demo project used for a presentation. Note. this may fall behind.
- The tests test most of the important features and should be fairly readable.

# Get it

We are using jitpack for releases currently; the nice thing is all I need to do is tag the release in Git and they do the rest. They have nice instructions for setting up your gradle or pom file:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

See [release notes](https://github.com/jillesvangurp/es-kotlin-wrapper-client/releases) with each github release tag for an overview what changed.

Note. this client assumes you are using this with Elasticsearch 6.x. The versions listed in our build.gradle and docker-compose file are what we test with. Usually we update to the latest version within days after Elasticsearch releases.

# Examples/highlights

The examples below are not the whole story. Please look at the tests for more details on how to use this and for working examples. I try to add tests for all the features along with lots of inline documentation. Also keeping markdown in sync with code is a bit of a PITA, so beware minor differences.

## Initialization

```kotlin
// we need the official client but we can create it with a new extension function
val esClient = RestHighLevelClient()

```

Or specify some optional parameters

```kotlin
// great if you need to interact with ES Cloud ...
val esClient = RestHighLevelClient(host="domain.com",port=9999,https=true,user="thedude",password="lebowski")
```

Or pass in the builder and rest client as you would normally.

**Everything** you do normally with the Java client works exactly as it does normally. But, we've added lots of extra methods to make things easier. For example searching is supported with a Kotlin DSL that adapts the existing `SearchRequest` and adds an alternative `source` method that takes a String, which in Kotlin you can template as well:

## DAOs and serialization/deserialization

A key feature in this library is using Data Access Objects or DAOs to abstract the business of using indices (and the soon to be removed types). Indices store JSON that conforms to your mapping. Presumably, you want to serialize and deserialize that JSON. DAOs take care of this and more for you.

You create a DAO per index:

```kotlin
// Lets use a simple Model class as an example
data class TestModel(var message: String)

// we use the refresh API in tests; you have to opt in to that being allowed explicitly 
// since you should not use that in production code.
val dao = esClient.crudDao<TestModel>("myindex", refreshAllowed = true,
modelReaderAndWriter = JacksonModelReaderAndWriter(
                TestModel::class,
                ObjectMapper().findAndRegisterModules()
))
```

The `modelReaderAndWriter` parameter takes care of serializing/deserializing. In this case we are using an implementation that uses Jackson and we are using the kotlin extension for that, which registers itself via `findAndRegisterModules`. 

Typically, most applications that use jackson, would want to inject their own custom object mappers. It's of course straightforward to use alternatives like GSon or whatever else you prefer. In the code below, we assume Jackson is used.

## CRUD with Entities

Managing documents in Elasticsearch basically means doing CRUD operations. The DAO supports this and makes it painless to manipulate documents.

```kotlin
// creates a document or fails if it already exists
// note you should probably apply a mapping to your 
// index before you index something ...
dao.index("xxx", TestModel("Hello World"))

// if you want to do an upsert, you can 
// do that with a index and setting create=false
dao.index("xxx", TestModel("Hello World"), create=false)
val testModel = dao.get("xxx")

// updates with conflict handling and optimistic locking
// you apply a lambda against an original 
// that is fetched from the index using get()
dao.update("xxx") { original -> 
  original.message = "Bye World"
}
// in case of a version conflict, update 
// retries. The default is 2 times 
// but you can override this.
// version conflicts happen when you
// have concurrent updates to the same document
dao.update("xxx", maxUpdateTries=10) { original -> 
  original.message = "Bye World"
}

// delete by id
dao.delete("xxx")
```
See [Crud Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/IndexDAOTest.kt) for more.

## Bulk Indexing using the Bulk DSL

The Bulk indexing API in Elasticsearch is complicated and it requires a bit of boiler plate to use and more boiler plate to use responsibly. This is made trivially easy with a `BulkIndexingSession` that abstracts all the hard stuff and a DSL that drives that:

```kotlin
// creates a BulkIndexingSession and uses it 
dao.bulk {
  // lets fill our index
  for (i in 0..1000000) {
    index("doc_$i", TestModel("Hi again for the $i'th time"))
  }
}
// when the block exits, the last 
// bulkRequest is send. 
// The BulkIndexingSession is AutoClosable.
```
See [Bulk Indexing Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/BulkIndexingSessionTest.kt) for more. 

There are many features covered there including per item callbacks to deal with success/failure per bulk item (rather than per page), optimistic locking for updates (using a callback), setting the bulk request page size, etc. You can tune this a lot but the defaults should be fine for simple usecases.

## Search

For search, the wrapper offers several useful features that make make constructing search queries and working with the response easier.

Given an index with some documents:

```kotlin
dao.bulk {
    index(randomId(), TestModel("the quick brown emu"))
    index(randomId(), TestModel("the quick brown fox"))
    index(randomId(), TestModel("the quick brown giraffe"))
    index(randomId(), TestModel("the quick brown horse"))
    index(randomId(), TestModel("lorem ipsum"))
    // throw in some more documents
    for(i in 0..100) {
        index(randomId(), TestModel("another document $i"))
    }
}
dao.refresh()
```

... lets do some searches. We want to find matching TestModel instances:

```kotlin
// our dao has a search short cut that does all the right things
val results = dao.search {
  // inside the block this is now the searchRequest, the index is already set correctly
  // you can use it as you would normally. Here we simply use the query DSL in the high level client.
  val query = SearchSourceBuilder.searchSource()
      .size(20)
      .query(BoolQueryBuilder().must(MatchQueryBuilder("message", "quick")))
  // set the query as the source on the search request
  source(query)
}

// SeachResults wraps the original SearchResponse and adds some features
// e.g. we put totalHits at the top level for convenience. 
assert(results.totalHits).isEqualTo(3L)

// results.searchHits is a Kotlin Sequence
results.searchHits.forEach { searchHit ->
    // the SearchHits from the elasticsearch client
}

// mappedHits maps the source using jackson, done lazily because we use a Kotlin sequence
results.mappedHits.forEach {
    // and we deserialized the results
    assert(it.message).contains("quick")
}
// or if you need both the original and mapped result, you can
results.hits.forEach {(searchHit,mapped) ->
    assert(mapped.message).contains("quick")
}
```

## Raw Json queries and multi line strings

The wrapper adds a few strategic extension methods. One of those takes care of the boilerplate you need to turn strings or streams into a valid query source.

So, the same search also works with multiline json strings in Kotlin and you don't have to jump through hoops to use raw json. Multiline strings are awesome in Kotlin and you can even inject variables and expressions.

```kotlin
val keyword="quick"
val results = dao.search {
    source("""
    {
      "size": 20,
      "query": {
        "match": {
          "message": "$keyword"
        }
      }
    }
    """)
}
```

In the same way you can also use `InputStream` and `Reader`. Together with some templating, this may be easier to deal with than programatically constructing complex queries via  builders. Up to you. I'm aware of some projects attempting a Kotlin query DSL and may end up adding support for that or something similar as well.

See [Search Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/SearchTest.kt) for more.

## Scrolling searches made easy

Scrolling is kind of tedious in Elastisearch. You have to keep track of scroll ids and get pages of results one by one. This requires boilerplate. We use Kotlin sequences to solve this. Sequences are lazy, so this won't run out of memory and you can safely stream process huge indiceses. Very nice in combination with bulk indexing.  

The Kotlin API is exactly the same as a normal paged search (see above). But please note that things like ranking don't work with scrolling searches and there are other subtle differences on the Elasticsearch side. 

```kotlin
// We can scroll through everything, all you need to do is set scrolling to true
// you can optionally override scrollTtlInMinutes, default is 1 minute
val results = dao.search(scrolling=true) {
  val query = SearchSourceBuilder.searchSource()
      .size(7) // lets use weird page size
      .query(matchAllQuery())
  source(query)
}

// Note: using the sequence causes the client to request for pages. 
// If you want to use the sequence again, you have to do another search.
results.mappedHits.forEach {
    println(it.message)
}
```


The `ScrollingSearchResults` implementation that is returned takes care of fetching all the pages, clearing the scrollId at the end, and of course mapping the hits to TestModel. You can only do this once of course since we don't keep the whole result set in memory and the scrollids are invalidated as you use them.

See [Search Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/SearchTest.kt) for more.

## Async with co-routines

The high level client supports asynchronous IO with a lot of async methods that take an `ActionListener`. We provide a `SuspendingActionListener` that you can use with these. Probably the most common use case is searching and for that we provide a convenient short hand:

```kotlin
val keyword="quick"
// you may want some more appropriate scope than global scope ...
runBlocking {
  val results = dao.searchAsync {
    source("""
      {
        "size": 20,
        "query": {
          "match": {
            "message": "$keyword"
          }
        }
      }
    """)
  }
}
```

Note, co-routines are a work in progress. 

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

While **this is a pre-1.0 version**, it should be perfectly fine for general use at this point. Also note, that you can always access the underlying Java client, which is stable. However, major refactorings do still happen ocassionally and there is still a lot to learn.

Kotlin recently added co-routines and using that is an obvious use case for Elasticsearch. Basic support for this has been added. However, several things in this client have yet to be updated to make full use of this. We expect the main use case for most users will be asynchronous searches, which is one of the things that are already fully supported.

If you want to contribute, please file tickets, create PRs, etc. For bigger work, please communicate before hand before committing a lot of your time.

## Compatibility

The general goal is to keep this client compatible with the current stable version of Elasticsearch. We rely on the most recent 6.x version. Presumably, this works fine against any 6.x node (the REST protocol should be more stable); and possibly some older versions. If you experience issues, please file a ticket or pull request.

Currently we update this libary regularly for the current stable version of Elasticsearch. With the upcoming 7.x versions, we may start having to do release branches for older versions. There have been minor Java API changes in the 6.x series in the client. As of v6.7.0, several things have been deprecated in the Java client and the current version of this client has  already addressed all relevant deprecations. A big upcoming change with v7 will be the deprecation and eventual removal of document types in Elasticsearch. So further API changes are likely though we expect this to be limited in scope.

As this is a development release, we still do fairly large changes and refactorings. Keep an eye on the release notes for this.

## Features (done)

- CRUD: index, update, get and delete of any jackson serializable object
- Reliable update with retries and optimistic locking that uses a `T -> T` lambda to transform what is in the index to what it needs to be. Retry kicks in if there's a version conflict and it simply re-fetches the latest version and applies the lambda.
- Bulk indexing with support for index, update, and delete. Supports callbacks for items and takes care of sending and creating bulk requests. The default callback can take care of retrying updates if they fail with a conflict if you set retryConflictingUpdates to a non zero value.
- Easy search with jackson based result mapping
- Easy scrolling search, also with jackson based result mapping. You can do this with the same method that does the normal paged search simply by setting `scrolling = true` (defaults to false)

## Future feature ideas/TODO/Doing

Mostly I develop on a need to have basis. The Elasticsearch API is enormous and there are many features I have never used. If it matters to you, feelf free to jump in and help.

Things currently on my horizon:

- Async / co-routines: this is in progress but not completed yet. There is a `SuspendingActionListener` that you can use with all the high level async requests. Currently, the only stuff using that is search (non scrolling only) and a handful of other requests. 
- Index and alias management with read and write alias support built in. In progress. The daos do support read and write aliases.
- ES 7.x branch - should be straightforward. Will probably start work on this close to the release.
- Cut down on the builder cruft and boilerplate in the query DSL and use extension methods with parameter defaults. Maybe adapt this project: https://github.com/mbuhot/eskotlin
- Set up CI, travis? Docker might be tricky.

# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is right. Forking is allowed and encouraged. I do appreciate attribution and pull requests...
