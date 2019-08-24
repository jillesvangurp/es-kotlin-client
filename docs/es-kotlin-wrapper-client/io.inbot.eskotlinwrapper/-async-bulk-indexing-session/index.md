[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [AsyncBulkIndexingSession](./index.md)

# AsyncBulkIndexingSession

`class AsyncBulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

Asynchronous bulk indexing that uses the experimental Kotlin flows. Works similar to the synchronous version except
it fires bulk requests asynchronously on the specified dispatcher. On paper using multiple threads, allows ES to
use multiple Threads to consume bulk requests.

Note: you need the kotlin.Experimental flag set for this to work. As Flow is a bit in flux, I expect the internals of this may change still before they finalize this. So, beware when using this.

### Functions

| Name | Summary |
|---|---|
| [delete](delete.md) | `fun delete(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, term: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, itemCallback: (`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Delete an object from the index. |
| [getAndUpdate](get-and-update.md) | `fun getAndUpdate(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, itemCallback: (`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Safe way to bulk update objects. Gets the object from the index first before applying the lambda to it to modify the existing object. If you set `retryConflictingUpdates` &gt; 0, it will attempt to retry to get the latest document and apply the `updateFunction` if there is a version conflict. |
| [index](--index--.md) | `fun index(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, obj: `[`T`](index.md#T)`, create: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, itemCallback: (`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [update](update.md) | `fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, primaryTerms: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, original: `[`T`](index.md#T)`, itemCallback: (`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Bulk update objects. If you have the object (e.g. because you are processing the sequence of a scrolling search), you can update what you have in a safe way.  If you set `retryConflictingUpdates` &gt; 0, it will retry by getting the latest version and re-applying the `updateFunction` in case of a version conflict. |

### Companion Object Functions

| Name | Summary |
|---|---|
| [asyncBulk](async-bulk.md) | `suspend fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> asyncBulk(client: RestHighLevelClient, dao: `[`IndexDAO`](../-index-d-a-o/index.md)`<`[`T`](async-bulk.md#T)`>, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](async-bulk.md#T)`> = dao.modelReaderAndWriter, bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](async-bulk.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, defaultRequestOptions: RequestOptions = dao.defaultRequestOptions, block: `[`AsyncBulkIndexingSession`](./index.md)`<`[`T`](async-bulk.md#T)`>.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`, bulkDispatcher: CoroutineDispatcher): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
