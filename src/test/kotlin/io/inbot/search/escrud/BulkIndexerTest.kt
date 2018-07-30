package io.inbot.search.escrud

import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

class BulkIndexerTest : AbstractElasticSearchTest() {

    @Test
    fun `should bulk index`() {
        val ids = mutableListOf<String>()
        for (i in 0..4) {
            ids.add(randomId())
        }

        dao.bulk(bulkSize = 2) {
            index(ids[0], TestModel("hi"))
            index(ids[1], TestModel("world"))
            index(ids[2], TestModel("."))
            index(ids[3], TestModel("!"), create = false)
            index(ids[3], TestModel("!!"))
            index(ids[3], TestModel("?"), create = true)
            index(ids[4], TestModel("and good bye"))
        }
        dao.refresh()
        assertk.assert(dao.get(ids[1])!!.message).isEqualTo("world")
        assertk.assert(dao.get(ids[3])!!.message).isEqualTo("!") // ! overwrote . but ? and !! failed because create was set to true
        assertk.assert(dao.get(ids[4])).isNotNull() // last item was processed
    }

    @Test
    fun `should do bulk update`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        dao.bulk() {
            getAndUpdate(id) { TestModel("${it.message} world!") }
        }
        dao.refresh()
        assertk.assert(dao.get(id)!!.message).isEqualTo("hi world!")
    }

    @Test
    fun `should retry bulk updates`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        dao.update(id) { foo -> TestModel("hi wrld") }
        val (doc, version) = dao.getWithVersion(id)!!
        dao.bulk(retryConflictingUpdates = 2) {
            // version here is wrong but we have retries set to 2 so it will recover
            update(id, version - 1, doc) { foo -> TestModel("omg") }
        }
        assertk.assert(dao.get(id)!!.message).isEqualTo("omg")
    }

    @Test
    fun `should bulk delete`() {
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
        assertk.assert(dao.get(ids[0])).isEqualTo(null)
    }
}