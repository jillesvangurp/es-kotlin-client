package io.inbot.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.elasticsearch.ElasticsearchStatusException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail

class AsyncIndexDAOTest : AbstractAsyncElasticSearchTest(indexPrefix = "crud") {
    @Test
    fun `index and delete a document`() {
        runBlocking {
            val id = randomId()
            dao.index(id, TestModel("hi"))
            assertThat(dao.get(id)).isEqualTo(TestModel("hi"))
            dao.delete(id)
            assertThat(dao.get(id)).isNull()
        }
    }

    @Test
    fun `update a document using a lambda function to transform what we have in the index`() {
        runBlocking {
            val id = randomId()
            dao.index(id, TestModel("hi"))
            // we use optimistic locking
            // this fetches the current version of the document and applies the lambda function to it
            dao.update(id) { TestModel("bye") }
            assertThat(dao.get(id)!!.message).isEqualTo("bye")
        }
    }

    @Test
    fun `Concurrent updates succeed and resolve conflicts without interference`() {
        runBlocking {

            val id = randomId()
            dao.index(id, TestModel("hi"))

            val jobs = mutableListOf<Deferred<Any>>()
            // do some concurrent updates; without retries this will fail
            for (n in 0.rangeTo(10)) {
                // this requires using multiple threads otherwise the failures will pile up
                jobs.add(async(Dispatchers.Unconfined) {
                    dao.update(id, 10) { TestModel("nr_$n") }
                })
            }
            awaitAll(*jobs.toTypedArray())
        }
    }

    @Test
    fun `produce conflict if you provide the wrong seqNo or primaryTerm`() {
        runBlocking {

            val id = randomId()
            // lets index a document
            dao.index(id, TestModel("hi"))

            try {
                // that version is wrong ...
                dao.index(id, TestModel("bar"), create = false, seqNo = 666, primaryTerm = 666)
                fail { "expected a version conflict" }
            } catch (e: Exception) {

            }
            // you can do manual optimistic locking
            dao.index(id, TestModel("bar"), create = false, seqNo = 0, primaryTerm = 1)
            assertThat(dao.get(id)!!.message).isEqualTo("bar")
        }
    }
}
