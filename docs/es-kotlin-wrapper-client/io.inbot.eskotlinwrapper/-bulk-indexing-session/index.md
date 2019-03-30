[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkIndexingSession](./index.md)

# BulkIndexingSession

`class BulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)

### Types

| Name | Summary |
|---|---|
| [BulkOperation](-bulk-operation/index.md) | `data class BulkOperation<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `BulkIndexingSession(client: RestHighLevelClient, dao: `[`IndexDAO`](../-index-d-a-o/index.md)`<`[`T`](index.md#T)`>, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>, bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT)` |

### Functions

| Name | Summary |
|---|---|
| [close](close.md) | `fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [delete](delete.md) | `fun delete(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, version: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [getAndUpdate](get-and-update.md) | `fun getAndUpdate(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [index](--index--.md) | `fun index(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, obj: `[`T`](index.md#T)`, create: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [update](update.md) | `fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, primaryTerms: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, original: `[`T`](index.md#T)`, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
