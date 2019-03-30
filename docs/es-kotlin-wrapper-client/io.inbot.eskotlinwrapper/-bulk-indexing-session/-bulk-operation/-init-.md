[es-kotlin-wrapper-client](../../../index.md) / [io.inbot.eskotlinwrapper](../../index.md) / [BulkIndexingSession](../index.md) / [BulkOperation](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`BulkOperation(operation: DocWriteRequest<*>, id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, updateFunction: ((`[`T`](index.md#T)`) -> `[`T`](index.md#T)`)? = null, itemCallback: (`[`BulkIndexingSession.BulkOperation`](index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = { _, _ -> })`

Bulk operation model used for e.g. `itemCallback`.

### Parameters

`T` - the type of the objects in the dao.