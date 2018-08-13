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

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/AbstractElasticSearchTest.kt
&lang=kotlin&title=Firing up some stuff

---?code=src/test/kotlin/io/inbot/eskotlinwrapper/IndexDAOTest.kt&lang=kotlin&title=Using the DAO
