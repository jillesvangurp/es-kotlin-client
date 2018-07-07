# Introduction

ES Kotlin Wrapper client for the Elasticsearch Highlevel REST client is an opinionated client for Elasticsearch.

# License

This project is licensed under the [MIT license](LICENSE).

# Motivation

I've been implementing my own http rest clients for various versions of Elasticsearch and have grown quite opionionated on the topic. Recently with v6, Elasticsearch released their own high level client. Unfortunately, it is still somewhat low level and actually merely exposes the Elasticsearch internal java api over http, which is why it pulls in most of ES.

So, I decided to fix things by using kotlin to wrap the various things their client provides and doing some sensible things with it.

# Development status

This is an early prototype that I did on a weekend. I'm planning to get a lot of code (re)written for inbot in the next weeks/months on top of this and will likely be adding, refactoring quite heavily. API compatibility is not going to be a concern short term. There will be bugs and other sillyness.

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
