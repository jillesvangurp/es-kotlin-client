[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client that wraps the official Highlevel Elasticsearch HTTP client (introduced with 6.x) with some Kotlin specific goodness. This adds convenience, cuts down on boilerplate, and makes using Elasticsearch safely easy and straightforward.

# Get it

I'm using jitpack for releases currently; the nice thing is all I need to do is tag the release in Git and they do the rest. They have nice instructions for setting up your gradle or pom file:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

This may change when this stuff becomes more stable. I'm planning to push this to maven central via Sonatype's OSS repository eventually. Such a PITA to set up and manage that I don't want to bother with that just yet. Ping me if this matters to you though.

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch (e.g. [this one](https://github.com/Inbot/inbot-es-http-client), that I unfortunately never had the chance to continue working on) and have grown quite opinionated on the topic. Recently with v6, Elasticsearch released their own high level rest client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of the elasticsearch java depedendencies. The internal java API is very complex and feature rich but a bit overkill for simple use cases.

Instead of writing yet another client, I decided to try to use it as is and instead wrap it with a convenient Kotlin API to add things that are needed. Kotlin makes this easy with language features such as, extension functions, builtin DSL support, reified generics, sequences, etc.

Key things I'm after in this project:

- Opinionated way of using Elasticsearch. I've used Elasticsearch for years, mainly using in house developed HTTP clients, and I like to use it in a certain way.
- Cover all the typical use cases around search that you need to worry about: managing indices, ingesting data, doing searches, scrolling through results, migrating indices, etc.
- Don't replace but enhance the official client; you can always resort to using that.
- Provide jackson support where relevant. XContent is not a thing in Spring Boot and most places that deal with Json in the Kotlin/Java world. Users should not have to deal with that.
- DRY & KISS. Get rid of boilerplate. Make the elasticsearch client easier to use for the standard usecases. 
- Use Kotlin language features to accomplish the above.


This library probably overlaps with several other efforts on Github. I'm aware of at least one attempt to do a Kotlin DSL for querying. I may end up pulling in these things or copying what they do.

# Example 

## Initialization

```kotlin
// the tests below use a simple entity class
data class TestModel(var message: String)

// next we need the official client
val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
val esClient = RestHighLevelClient(restClientBuilder)

// and next we create an indexDao for our model class.
// there's a few extension methods that add new features to the client and elsewhere
val dao = esClient.crudDao<TestModel>("myindex", refreshAllowed = true)
```

## Crud

```kotlin
dao.index("xxx", TestModel("Hello World"))

val testModel = dao.get("xxx")

// updates with conflict handling and optimistic locking
// you apply a lambda against an original that is fetched from the index using get()
dao.update("xxx") { original -> 
  original.message = "Bye World"
}
// in case of a version conflict, it retries. The default is 2 times but you can override this.
// version conflicts happen when you have concurrent updates to the same document
dao.update("xxx", maxUpdateTries=10) { original -> 
  original.message = "Bye World"
}

// deletes
dao.delete("xxx")
```
See [Crud Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/IndexDAOTest.kt) for more.

## Bulk Indexing

```kotlin
dao.bulk {
  // lets fill our index
  for (i in 0..1000000) {
    // inside the block, this refers to the BulkIndexingSession instance that bulk manages for you
    // The BulkIndexingSession creates BulkRequests on the fly and sends them off 100 operations at the time 
    // index, update, updateAndGet, and delete are functions that the BulkIndexingSession exposes.
    // this indexes a document
    index("doc_$i", TestModel("Hi again for the $i'th time"))
  }
}
// when the bulk block exits, the last bulkRequest is send. BulkIndexingSession is AutoClosable.
```
See [Bulk Indexing Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/BulkIndexingSessionTest.kt) for more

## Search

```kotlin
// lets put some documents in an index
dao.bulk {
    index(randomId(), TestModel("the quick brown emu"))
    index(randomId(), TestModel("the quick brown fox"))
    index(randomId(), TestModel("the quick brown horse"))
    index(randomId(), TestModel("lorem ipsum"))
    // throw in some more documents
    for(i in 0..100) {
        index(randomId(), TestModel("another document $i"))
    }
}
dao.refresh()


// initiate a search against the dao's index for TestModels that match the criteria
val results = dao.search {
  // this is now the searchRequest, the index is already set correctly
  val query = SearchSourceBuilder.searchSource()
      .size(20)
      .query(BoolQueryBuilder().must(MatchQueryBuilder("message", "quick")))
  // set the query as the source on the search request
  source(query)
}

// SeachResults wrap the original SearchResponse
// we put totalHits at the top level for convenience
assert(results.totalHits).isEqualTo(3L)

results.hits.forEach {
    // and we deserialized the results
    assert(it.message).contains("quick")
}

```

The same search also works with multiline json strings in Kotlin so you don't have to jump through hoops to use raw json:

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

See [Search Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/SearchTest.kt) for more.


## Scrolling searches

Scrolling is kind of tedious in Elastisearch. We use Kotlin sequences to solve this.

```kotlin
// We can also scroll through everything, all you need to do is set scrolling to true
// you can optionally override scrollTtlInMinutes, default is 1 minute
val results = dao.search(scrolling=true) {
  // this is now the searchRequest, the index is already set correctly
  val query = SearchSourceBuilder.searchSource()
      .size(7) // lets use weird page size
      .query(matchAllQuery())
  // set the query as the source on the search request
  source(query)
}

// Note: this consumes the sequence. If you want to use the sequence again, you have to do another search
results.hits.forEach {
    println(it.message)
}
```

The `ScrollingSearchResults` implementation that is returned takes care of fetching all the pages, clearing the scrollId at the end, and of course mapping the hits to TestModel

See [Search Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/eskotlinwrapper/SearchTest.kt) for more.

# Building

You need java >= 8 and docker + docker compose.

Simply use the gradle wrapper to build things:

```
./gradlew build
```

Look inside the build file for comments and documentation.

It will spin up elasticsearch using docker compose and run the tests. If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster.

# Changelog

- 0.9.1 Add raw json support for constructing queries.
- 0.0.1 - 0.9.0 Search/Scrolling search, bulk indexing, crud. Not feature complete but things are usable and useful.

# Development status

**This is a work in progress**. This is an alpha version. I'm still adding features, refactoring, doing API and package renames, etc. When this hits 1.0 things will get more stable.

That being said, the core feature set is there, works, and is probably highly useful if you need to talk to Elasticsearch from Kotlin or Java (you may run into some Kotlin weirdness). 

Your feedback, issues, PRs, etc. are appreciated. If you do use it in this early stage, let me know so I don't accidentally make you unhappy by refactoring stuff you use.

## Features (done)

- CRUD: index, update, get and delete of any jackson serializable object
- Reliable update with retries and optimistic locking that uses a `T -> T` lambda to transform what is in the index to what it needs to be. Retry kicks in if there's a version conflict and it simply re-fetches the latest version and applies the lambda.
- Bulk indexing with support for index, update, and delete. Supports callbacks for items and takes care of sending and creating bulk requests. The default callback can take care of retrying updates if they fail with a conflict if you set retryConflictingUpdates to a non zero value.
- Easy search with jackson based result mapping
- Easy scrolling search, also with jackson based result mapping. You can do this with the same method that does the normal paged search simply by setting `scrolling = true` (defaults to false)

## TODO

- Cut down on the builder cruft and boilerplate in the query DSL and use extension methods with parameter defaults.
- Make creating and using aggregations less painful. 
- Index and alias management
- Schema migration support
- API documentation, mostly straightforward
- Set up CI, travis? Docker might be tricky.

# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is right. Forking is allowed and encouraged. I do appreciate attribution and pull requests...
