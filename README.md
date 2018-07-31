[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client that wraps the official Highlevel Elasticsearch HTTP client (introduced with 6.x) with some Kotlin specific goodness. This adds convenience, cuts down on boilerplate, and makes using Elasticsearch safely easy and straightforward.

# Get it

I'm using jitpack for releases currently. They have nice instructions:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

This may change when this stuff becomes more stable.

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch (e.g. [this one](https://github.com/Inbot/inbot-es-http-client), that I unfortunately never had the chance to continue working on) and have grown quite opinionated on the topic. Recently with v6, Elasticsearch released their own high level rest client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of the elasticsearch java depedendencies. The internal java API is very complex and feature rich but a bit overkill for the simple use cases.

Instead of writing yet another client, I decided to try to use it as is instead wrap it with a convenient Kotlin API to add things that are needed. Kotlin makes this easy with language features such as, extension functions, builtin DSL support, reified generics, sequences, etc.

Key things I'm after in this project:

- Opinionated way of using Elasticsearch. I've used Elasticsearch for years, mainly using in house developed HTTP clients, and I like to use it in a certain way.
- Provide jackson support where relevant. XContent is not a thing in Spring Boot and most places that deal with Json in the Kotlin/Java world. Users should not have to deal with that.
- DRY & KISS. Get rid of boilerplate. Make the elasticsearch client easier to use for standard usecases. 
- Use Kotlin language features to accomplish the above.

This library probably overlaps with several other efforts on Github. I'm aware of at least one attempt to do a Kotlin DSL for querying.

# Development status

**This is a work in progress**. I will update this readme when this changes. 

I'm planning to get a lot of code (re)written for Inbot in the next weeks/months on top of this and will likely be adding new features and be refactoring quite heavily as I start doing that. API compatibility is not going to be a goal short term. As my own code base grows, this library will change less frequently and less drastically. At some point I will slap a `1.0` on it when I feel that things are stable enough. There will be bugs and other sillyness. Finally, I've only been using Kotlin for a few months and am constantly discovering useful ways to abuse various language features. E.g. reified generics, extension functions, lazy properties, etc.

Your feedback, issues, PRs, etc. are appreciated. If you do use it in this early stage, let me know so I don't make you unhappy by refactoring stuff you use.

# Features (done)

- CRUD: index, update, get and delete of any jackson serializable object
- Reliable update with retries and optimistic locking that uses a `T -> T` lambda to transform what is in the index to what it needs to be. Retry kicks in if there's a version conflict and it simply re-fetches the latest version and applies the lambda.
- Bulk indexer with support for index, update, and delete. Supports callbacks for items and takes care of sending and creating bulk requests. The default callback can take care of retrying updates if they fail with a conflict if you set retryConflictingUpdates to a non zero value.
- Easy search with jackson based result mapping

# TODO

- Cut down on the builder cruft and boilerplate in the query DSL and use extension methods with parameter defaults.
- Make creating and using aggregations less painful. 
- Turn scrolling searches in a kotlin Sequence that deals with paging so you can do something like this: `dao1.bulk { dao2.searchAll().forEach { index(transform(it))} }` to pump data from one index to another while transforming the data. Doing this with the current client requires a lot of boiler plate. 


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
See [Crud Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/ElasticSearchCrudServiceTests.kt) for more.

## Bulk Indexing

```kotlin
dao.bulk {
  // lets fill our index
  for (i in 0..1000000) {
    // inside the block, this refers to the BulkIndexer instance that bulk manages for you
    // The BulkIndexer creates BulkRequests on the fly and sends them off 100 operations at the time 
    // index, update, updateAndGet, and delete are functions that the BulkIndexer exposes.
    // this indexes a document
    index("doc_$i", TestModel("Hi again for the $i'th time"))
  }
}
// when the bulk block exits, the last bulkRequest is send. BulkIndexer is AutoClosable.
```
See [Bulk Indexing Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/BulkIndexerTest.kt) for more

## Search

```kotlin
// lets put some documents in an index
dao.bulk {
    index(randomId(), TestModel("the quick brown emu"))
    index(randomId(), TestModel("the quick brown fox"))
    index(randomId(), TestModel("the quick brown horse"))
    index(randomId(), TestModel("lorem ipsum"))
}
dao.refresh()


// get SearchResults with our DSL
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
assert(results.totalHits).isEqualTo(results.searchResponse.hits.totalHits).isEqualTo(3L)
results.hits.forEach {
    // and we deserialized the results
    assert(it.message).contains("quick")
}
```

See [Search Tests](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/SearchTest.kt) for more.

# Building

You need java >= 8  and docker + docker compose.

Simply use the gradle wrapper:

```
./gradlew build
```

Look inside the build file for comments and documentation.

It will spin up elasticsearch using docker compose and run the tests. If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster.

# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is right. Forking is allowed and encouraged. I do appreciate attribution and pull requests...
