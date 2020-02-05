package io.inbot.eskotlinwrapper

import org.elasticsearch.action.DocWriteRequest
import org.elasticsearch.action.bulk.BulkItemResponse

/**
 * Bulk operation model used for e.g. `itemCallback`.
 *
 * @param T the type of the objects in the dao.
 */
data class BulkOperation<T : Any>(
    val operation: DocWriteRequest<*>,
    val id: String,
    val updateFunction: ((T) -> T)? = null,
    val itemCallback: (BulkOperation<T>, BulkItemResponse) -> Unit = { _, _ -> }
)

data class AsyncBulkOperation<T : Any>(
    val operation: DocWriteRequest<*>,
    val id: String,
    val updateFunction: suspend ((T) -> T) = {it},
    val itemCallback: suspend (AsyncBulkOperation<T>, BulkItemResponse) -> Unit = { _, _ -> }
)
