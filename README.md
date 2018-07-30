[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client for Elasticsearch.

# Get it

I'm using jitpack for releases currently. They have nice instructions:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

This may change when this stuff becomes more stable.

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch and have grown quite opinionated on the topic. Recently with v6, Elasticsearch released their own high level rest client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of the elasticsearch java depedendencies. The internal java API is very complex and feature rich but a bit overkill for the simple use cases.

Instead of writing yet another client, I decided to try to use it and instead wrap it with a convenient Kotlin API. Of course given that I use Kotlin, I can fix things myself by using kotlin to wrap the various things their client provides and doing some sensible things with it extension functions, the kotlin primitives for building DSLs, etc.

The advantage of this approach is that you gain easy to use API and can fall back to the official API when you need to. Also, this makes it easy to use new features as Elasticsearch adds them.

Key things I'm after in this project:

- Opinionated way of using Elasticsearch. I've used Elasticsearch for a while and I like to use it in a certain way. Mainly this involves using aliases, index management, having a dao abstraction, low amount of boilerplate for things that you necessarily do a lot like bulk indexing, searching for stuff, etc.
- Provide jackson support where relevant. XContent is not a thing in Spring Boot and most places that deal with JSon. Users should not have to deal with that.
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

Code below liberally copy pasted from the tests, refer to the tests for working code.

## Initialization

```kotlin
// the tests below use a simple entity class
data class TestModel(var message: String)

// this is how you initialize 
val restClientBuilder = RestClient.builder(HttpHost("localhost", 9200, "http"))
val esClient = RestHighLevelClient(restClientBuilder)

// there's a few extension methods that add new features to the client and elsewhere
val dao = esClient.crudDao<TestModel>("myindex", refreshAllowed = true)
```

## Examples

To see how it works, simply look at the tests. I may at some point write more comprehensive documentation.   

- [Crud](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/ElasticSearchCrudServiceTests.kt)
- [Bulk Indexing](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/BulkIndexerTest.kt)
- [Searching for stuff](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/src/test/kotlin/io/inbot/search/escrud/SearchTest.kt) 


# Building

You need java >= 8  and docker + docker compose.

Simply use the gradle wrapper:

```
./gradlew build
```

It will spin up elasticsearch using docker compose and run the tests. If you want to run the tests from your IDE, just use `docker-compose up -d` to start ES. The tests expect to find that on a non standard port of `9999`. This is to avoid accidentally running tests against a real cluster.

# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is right. Forking is allowed and encouraged. I do appreciate attribution and pull requests...
