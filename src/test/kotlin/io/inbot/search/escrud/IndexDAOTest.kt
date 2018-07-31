package io.inbot.search.escrud

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.elasticsearch.ElasticsearchStatusException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IndexDAOTest : AbstractElasticSearchTest(indexPrefix = "crud") {
    @Test
    fun `index and delete a document`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        assert(dao.get(id)).isEqualTo(TestModel("hi"))
        dao.delete(id)
        assert(dao.get(id)).isNull()
    }

    @Test
    fun `update a document using a lambda function to transform what we have in the index`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        // we use optimistic locking
        // this fetches the current version of the document and applies the lambda function to it
        dao.update(id) { TestModel("bye") }
        assert(dao.get(id)!!.message).isEqualTo("bye")
    }

    @Test
    fun `Concurrent updates succeed and resolve conflicts without interference`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        // do some concurrent updates; without retries this will fail
        0.rangeTo(30).toList().parallelStream().forEach { n ->
            // the maxUpdateTries parameter is optional and has a default value of 2
            // you will see in the logs that it actually has to retry a few times
            dao.update(id, 10) { TestModel("nr_$n") }
        }
    }

    @Test
    fun `produce version conflict if you provide the wrong version`() {
        val id = randomId()
        // lets index a document
        dao.index(id, TestModel("hi"))

        assertThrows<ElasticsearchStatusException> {
            // that version is wrong ...
            dao.index(id, TestModel("bar"), create = false, version = 666)
        }
        // you can do manual optimistic locking
        dao.index(id, TestModel("bar"), create = false, version = 1)
    }
}
