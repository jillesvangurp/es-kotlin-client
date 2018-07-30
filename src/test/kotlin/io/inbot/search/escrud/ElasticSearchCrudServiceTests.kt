package io.inbot.search.escrud

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.elasticsearch.ElasticsearchStatusException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID


class ElasticSearchCrudServiceTests : AbstractElasticSearchTest() {
    @Test
    fun `should index`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        assert(dao.get(id)).isEqualTo(TestModel("hi"))
        dao.delete(id)
        assert(dao.get(id)).isNull()
    }

    @Test
    fun `should update`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        dao.update(id) { TestModel("bye") }
        assert(dao.get(id)!!.message).isEqualTo("bye")
    }

    @Test
    fun `should produce version conflict`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))

        assertThrows<ElasticsearchStatusException> {
            dao.index(id, TestModel("bar"), create = false, version = 3)
        }
    }

    @Test
    fun `should do concurrent updates`() {
        val id = randomId()
        dao.index(id, TestModel("hi"))
        // do some concurrent updates; without retries this will fail
        0.rangeTo(30).toList().parallelStream().forEach { n ->
            dao.update(id, 10) { TestModel("nr_$n") }
        }
    }
}
