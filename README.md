[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)


# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client for Elasticsearch.

# Get it

I'm using jitpack for releases currently. They have nice instructions:

[![](https://jitpack.io/v/jillesvangurp/es-kotlin-wrapper-client.svg)](https://jitpack.io/#jillesvangurp/es-kotlin-wrapper-client)

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch and have grown quite opionionated on the topic. Recently with v6, Elasticsearch released their own high level client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of ES.

So, I decided to fix things by using kotlin to wrap the various things their client provides and doing some sensible things with it.

Key things I'm after in this project:

- provide opinionated way of dealing with anything elasticsearch
- reuse as much of the DSL work in the elasticsearch client but make that a bit easier to use perhaps
- add DAOs that do the right things with version checks, updates, creates, bulk behavior with the minimum amount of boilerplate
- port over alias and schema management that I've used in Inbot for some years.
- be jackson object mapper friendly, users should NEVER have to deal with XContent or any other low level stuff that the 'high' level client exposes.
- be jsonj friendly too; this is my jackson add on that makes prototyping with json a bit easier. Jsonj will not be a required dependency though.
- be kotlin friendly and centric. I write all new Java stuff in Kotlin these days. I'll try to keep this usable from Java though.


# Development status

This is an early prototype that I did on a weekend. **It's very much a work in progress**. I will update this readme when this changes. 

I'm planning to get a lot of code (re)written for inbot in the next weeks/months on top of this and will likely be adding, refactoring quite heavily. API compatibility is not going to be a concern short term. There will be bugs and other sillyness.

It probably overlaps with several other efforts on Github. 

But your feedback, PRs, etc. are appreciated; I just want to avoid people depending on this while I'm still figuring out what to do with it.





# Example 

Code below liberally copy pasted from the tests, refer to the tests for working code.

```kotlin
// create the official client
val esClient = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))

// get a jackson object mapper
val objectMapper = ObjectMapper().findAndRegisterModules()

// create a DAO for the test index that will store Foo objects
val dao = ElasticSearchCrudDAO<Foo>("test", Foo::class, esClient, objectMapper)

val id = randomId()

// create a new object, will fail if it already exists
dao.index(id, Foo("hi"))
dao.get(id) shouldBe Foo("hi")
dao.delete(id)
dao.get(id) shouldBe null

// this will eventually also deal with version conflicts and retry a couple of times
dao.update(id) { Foo("bye") }
dao.get(id)!!.message shouldBe "bye"

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
