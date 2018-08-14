# ES Kotlin Wrapper
## for the Elasticsearch Highlevel REST Client for Java

---

- brief history of me & ES
- why
- how
- demo

---
- Es < 5.x
  - No http client for java
  - Talk to ES by firing up a cluster node inside your application
  - Use internal Java APIs that use the binary cluster protocol
- Es 5.x low level http client
  - Internal java api deprecated for client usage
- Es 6.x
  - High level client

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/AbstractElasticSearchTest.kt&lang=kotlin&title=Create the client
@[18-19](create the Java client)
@[22-27](now create a DAO)

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/TestModel.kt&lang=kotlin&title=A simple Entity

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/IndexDAOTest.kt&lang=kotlin&title=Using the DAO
@[13-17](Create a document in the index)
@[26](Updates)
@[35-39](Updates with retry and optimistic locking)
@[35-39](Updates with retry and optimistic locking)

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/BulkIndexingSessionTest.kt&lang=kotlin&title=Bulk
