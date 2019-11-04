package io.inbot.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.elasticsearch.action.support.WriteRequest
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
class BulkIndexingSessionTest : AbstractElasticSearchTest(indexPrefix = "bulk") {

    @Test
    fun `This is how you bulk index some documents`() {
        val ids = mutableListOf<String>()
        for (i in 0..4) {
            ids.add(randomId())
        }

        // we have paging, the bulkIndexer will send a BulkRequest every 2 operations. You don't have to worry about that.
        // we have a sane default for the refreshPolicy. If you don't do this, you risk filling up the server side queues.
        dao.bulk(bulkSize = 2) {
            // 'this' is a BulkIndexingSession in the block
            index(ids[0], TestModel("hi"))
            index(ids[1], TestModel("world"))
            index(ids[2], TestModel("."))
            // some of these items will fail
            index(ids[3], TestModel("!"), create = false)
            index(ids[3], TestModel("!!"))
            index(ids[3], TestModel("?"), create = true)
            index(ids[4], TestModel("and good bye"))
        }
        dao.refresh()
        // verify we got what we expected
        assertThat(dao.get(ids[1])!!.message).isEqualTo("world")
        // ! overwrote . but ? and !! failed because create was set to true
        assertThat(dao.get(ids[3])!!.message).isEqualTo("!")
        // last item was processed
        assertThat(dao.get(ids[4])).isNotNull()
    }

    @RepeatedTest(4) // this used to be flaky until I fixed it; so run multiple times to ensure we don't break it again
    fun `We also support bulk updates`() {
        val id = randomId()
        val id2 = randomId()
        // index a document
        dao.index(id, TestModel("hi"))
        val original2 = TestModel("bye")
        dao.index(id2, original2)
        val getResponse = dao.getWithGetResponse(id2)?.second ?: fail { "should not return null" }
        // these vary depending on which shard is used so don't hard code these and look them up
        val seqNo = getResponse.seqNo
        val primaryTerm = getResponse.primaryTerm

        dao.bulk(refreshPolicy = WriteRequest.RefreshPolicy.IMMEDIATE) {
            // gets and updates the document
            getAndUpdate(id) { TestModel("${it.message} world!") }
            // or you can fetch the original yourself and specify the seq_no and primary terms manually
            // normally you'd be using a scrolling search to fetch and bulk update
            // see bulk indexer for that
            update(id2, seqNo, primaryTerm, original2) { d2 ->
                d2.message = d2.message + " world!"
                d2
            }
            // TODO upsert
        }
        assertThat(dao.get(id)!!.message).isEqualTo("hi world!")
        assertThat(dao.get(id2)!!.message).isEqualTo("bye world!")
    }

    @Test
    fun `When updates fail they are retried`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        dao.update(id) { _ -> TestModel("hi wrld") }
        val (doc, _) = dao.getWithGetResponse(id)!!
        // note this is actually the default but setting it explicitly for clarity
        dao.bulk(retryConflictingUpdates = 2) {
            // seq no and primary term are wrong, so it should recover
            update(id, 666, 666, doc) { _ -> TestModel("omg") }
        }
        assertThat(dao.get(id)!!.message).isEqualTo("omg")
    }

    @Test
    fun `And you can bulk delete of course`() {
        val ids = mutableListOf<String>()
        dao.bulk() {
            for (i in 0..4) {
                val id = randomId()
                ids.add(id)
                index(id, TestModel(id))
            }
            ids.forEach { delete(it) }
        }
        dao.refresh()
        assertThat(dao.get(ids[0])).isEqualTo(null)
    }

    @Test
    fun `We have callbacks so you can act if something happens with one of your bulk operations`() {
        val successes = mutableListOf<Any>()
        dao.bulk {
            // retries and logging are done via callbacks; if you want, you can override these and do something custom
            this.index(
                randomId(),
                TestModel("another object"), itemCallback = { operation, response ->
                    if (response.isFailed) {
                        // do something custom
                    } else {
                        // lets just add the operation to a list
                        successes.add(operation)
                    }
                })
        }
        assertThat(successes).hasSize(1)
    }

    @Test
    fun `Instead of per operation callbacks you can also just use the same one for all operations`() {
        val successes = mutableListOf<Any>()
        // instead of haveing a per operation callback, you can also tell the bulkIndexer to always use the same lambda
        dao.bulk(itemCallback = { operation, response ->
            if (response.isFailed) {
                // do something custom
            } else {
                successes.add(operation)
            }
        }) {
            this.index(randomId(), TestModel("another object"))
            this.index(randomId(), TestModel("and another object"))
        }
        assertThat(successes).hasSize(2)
    }

    @Test
    fun `async bulk test`() {
        val successes = mutableListOf<Any>()
        runBlocking {
            val totalItems = 10000
            dao.bulkAsync(
                bulkSize = 200,
                refreshPolicy = WriteRequest.RefreshPolicy.NONE,
                itemCallback = { operation, response ->
                    if (response.isFailed) {
                        println(response.failureMessage)
                    } else {
                        // this only gets called if ES reports back with a success response
                        successes.add(operation)
                    }
                }
            ) {
                (0 until totalItems).forEach {
                    index(randomId(), TestModel("object $it"))
                }
            }
            // ES has confirmed we have the exact number of items that we bulk indexed
            assertThat(successes).hasSize(totalItems)
        }
    }
}
