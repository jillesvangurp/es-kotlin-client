[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client for Elasticsearch.

# Get it

I'm using jitpack for releases currently. They have nice instructions:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch and have grown quite opinionated on the topic. Recently with v6, Elasticsearch released their own high level rest client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of the elasticsearch java depedendencies.

Instead of writing yet another client, I decided to try to use it and instead wrap it with a convenient Kotlin API. Of course given that I use Kotlin, I can fix things myself by using kotlin to wrap the various things their client provides and doing some sensible things with it extension functions, the kotlin primitives for building DSLs, etc.

Key things I'm after in this project:

- provide opinionated way of dealing with anything elasticsearch. This runs as part of our production code and needs to be robust, easy to use, and not result in a lot of copy paste style 'reuse' to deal with all the boiler plate for error handling, setting things up, etc.
- Reuse as much of the DSL work in the elasticsearch client but make that a bit easier to use perhaps.
- add a DAO abstraction that does the right things with version checks/optimistic locking, updates, creates, bulk behavior, etc. with the minimum amount of boilerplate
- port over alias and schema management that I've used in Inbot for some years and support migrations in a sane way
- be jackson object mapper friendly; users should NEVER have to deal with XContent or any other low level stuff that the 'high' level client exposes and insists on.
- be kotlin friendly and centric. I write all new Java stuff in Kotlin these days. I'll try to keep this usable from Java though but it is not a goal or something I test.

# Development status

**This is a work in progress**. I will update this readme when this changes. 

I'm planning to get a lot of code (re)written for inbot in the next weeks/months on top of this and will likely be adding new features and be refactoring quite heavily. API compatibility is not going to be a goal short term. Also, there will be bugs and other sillyness.

It probably overlaps with several other efforts on Github. 

But your feedback, PRs, etc. are appreciated; I just want to avoid people depending on this while I'm still figuring out what to do with it.

# Features (done)

`ElasticSearchCrudDAO`:

- index, get and delete of any jackson serializable object
- reliable update with retries and optimistic locking that uses a `T -> T` lambda to transform what is in the index to what it needs to be. Retry kicks in if there's a version conflict and it simply re-fetches the latest version and applies the lambda.
- bulk indexer with support for index, update, and delete. Supports callbacks for items and takes care of sending and creating bulk requests. The default callback can take care of retrying updates if they fail with a conflict if you set retryConflictingUpdates to a non zero value.


# Example 

Code below liberally copy pasted from the tests, refer to the tests for working code.

## Creating an ElasticSearchCrudDAO

```kotlin
// create the official client
val esClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))

// get a jackson object mapper
val objectMapper = ObjectMapper().findAndRegisterModules()

// create a DAO for the test index that will store Foo objects
// types are deprecated in ES so I default to simply using the index as the type
val dao = ElasticSearchCrudDAO<Foo>("testindex", Foo::class, esClient, objectMapper)
// you can add a type of course, if you want
val dao = ElasticSearchCrudDAO<Foo>("testindex", Foo::class, esClient, objectMapper, type="icanhastypes")

// OR
val dao = esClient.crudDao<Foo>("testindex")

// OR override some defaults
val dao = esClient.crudDao<Foo>("testindex",objectMapper=objectMapper, refreshAllowed=true)
```

This stuff probably goes in your spring configuration or whatever DI framework you use.

## Simple Crud

```kotlin
// lets use a simple entity class
data class Foo(var message: String)

val id = randomId()

// create a new object, will fail if it already exists
dao.index(id, Foo("hi"))
// fails because we have create=true by default
dao.index(id, Foo("hi again"))  
// now it succeeds
dao.index(id, Foo("hi again"),create=false)  
// returns a Foo
val aFoo = dao.get(id) 
// useful if you are doing bulk updates
val (aFoo,version) = dao.getWithVersion(id)
dao.delete(id)
// returns null
dao.get(id) 

// updates apply a lambda function to the original in the index
// update also deals with version conflicts and retries a configurable number of times (default 2) with a sleep to reduce chance of more conflicts
dao.update(id, maxUpdateRetries=5) { it.message="bye" }
dao.get(id)!!.message shouldBe "bye"
```

## Bulk DSL

```kotlin
dao.bulk(retryConflictingUpdates=2) {
  index("1", Foo("hello"))
  index("2", Foo("world"))
  index("3", Foo("wrld"))
  // fails becaue create=true by default
  index("3", Foo("world")) 
  // succeeds because it overwrites the original
  index("3", Foo("world"), create=false) 

  // updates the original that we get from the index using the update lambda
  getAndUpdate("1",{originalFoo -> Foo("hi")}
  // you can also look up the current version yourself; note you have to provide a version
  // this succeeds because we can retry even though the version is wrong here. 
  // retry simply falls back to a non bulk update and re-applies the lambda to the latest version in the index
  update("2",666,Foo("hi"), {foo -> Foo("hi wrld")}
}
```

# Building

You need java 8 and docker + docker compose.

Simply use the gradle wrapper:

```
./gradlew build
```

It will spin up elasticsearch using docker compose and run the tests.

# License

This project is licensed under the [MIT license](LICENSE). This maximizes everybody's freedom to do what needs doing. Please exercise your rights under this license in any way you feel is right. I do appreciate attribution ...
