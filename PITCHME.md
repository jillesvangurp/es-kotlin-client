# Kotlin Wrapper
#### for the Elasticsearch Highlevel REST Client for Java
### Jilles van Gurp

---
# Elasticsearch

- You know, for search.
  - https://www.elastic.co/
- JSON Document store
- Search, aggregations, did you mean, etc.
- REST based API
- Lots of API clients for different languages
- Lots of sites with a searchbox use ES

---
# ES HTTP API

- CRUD for JSON documents
- Search + JSON query DSL
- Bulkindexing, index and alias management, aggregations
- lots more

---
# My ES Clients
- Built my first ES client 5 years ago. For ES 1.7
  - Used HTTP API instead of internal cluster protocol
  - Because ES did not have a HTTP API client for Java
- My second attempt 2016. Es 2.x
  - Still no HTTP based API client for Java
  - OSS abandonware on github: https://github.com/Inbot/inbot-es-http-client
- Es Kotlin Wrapper!
  - ES 6.x finally added a Java HTTP client. Binary protocol is deprecated
  - Kind of complicated to use; lots of boilerplate needed.
  - Lacks all the features I built earlier
  - https://github.com/jillesvangurp/es-kotlin-wrapper-client

---
# What does it do?

- Uses a simple DAO abstraction for indices
  - CRUD simple documents in an index
  - Sane way to do bulk indexing
  - Easy way to do searches
  - Easy way to do scrolling searches

---
# Enough bullets
- Code & Demos

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/AbstractElasticSearchTest.kt&lang=kotlin&title=Create the client
@[18-19](create the Java client)
@[22-27](now create a DAO)

---?code=src/main/kotlin/io/inbot/eskotlinwrapper/ModelReaderAndWriter.kt&lang=kotlin&title=Serialization

---?code=src/main/kotlin/io/inbot/eskotlinwrapper/JacksonModelReaderAndWriter.kt&lang=kotlin&title=Default implementation for jackson

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/TestModel.kt&lang=kotlin&title=A simple Entity
@[4](Doesn't get any simpler)

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/IndexDAOTest.kt&lang=kotlin&title=Using the DAO
@[13-17](Create a document in the index)
@[26](Updates)
@[35-39](Updates with retry and optimistic locking)
@[48-51](It can fail if you are out of date)
@[53](And if you specify the correct version it works)

---?code=src/main/kotlin/io/inbot/eskotlinwrapper/IndexDAO.kt&lang=kotlin&title=DAO Implementation

@[28](use kotlin default arguments for sane defaults)
@[34](here's our serializer)
@[43](update hands of to a private method that can retry)
@[49](fetch current version)
@[56](update is just another index)
@[66-73](but we handle conflicts by retrying)

---?code=src/main/kotlin/io/inbot/eskotlinwrapper/RestHighLevelClientExtensions.kt&lang=kotlin&title=Add Dao creation to the client

@[25-31](but we handle conflicts by retrying)

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/BulkIndexingSessionTest.kt&lang=kotlin&title=Bulk
