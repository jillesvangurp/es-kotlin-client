[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkIndexingSession](./index.md)

# BulkIndexingSession

`class BulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)

Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](../-index-d-a-o/index.md). Implements `AutoCloseable` to ensure all operations are processed.

```
dao.bulk() {
  index("xxx",myObject)
  index("yyy",anotherObject)
  delete("zzz")*
}
```

### Parameters

`client` -

`dao` -

`modelReaderAndWriter` - Defaults to the one configured on the dao.

`bulkSize` - override this to change the bulk page size (the number of items sent to ES with one request).

`retryConflictingUpdates` - the default `itemCallback` is capable of retrying updates. When retrying it will get the document and try again. The default for this is 0.

`refreshPolicy` - The bulk API returns a response that contains a per item response. This callback facilitates dealing with e.g. failures. The default implementation does logging and update retries.

`itemCallback` - Override request options if you need to. Defaults to those configured on the dao.

`defaultRequestOptions` - Defaults to what you configured on the `dao`

### Types

| Name | Summary |
|---|---|
| [BulkOperation](-bulk-operation/index.md) | `data class BulkOperation<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>Bulk operation model used for e.g. `itemCallback`. |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `BulkIndexingSession(client: RestHighLevelClient, dao: `[`IndexDAO`](../-index-d-a-o/index.md)`<`[`T`](index.md#T)`>, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`> = dao.modelReaderAndWriter, bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, defaultRequestOptions: RequestOptions = dao.defaultRequestOptions)`<br>Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](../-index-d-a-o/index.md). Implements `AutoCloseable` to ensure all operations are processed. |

### Functions

| Name | Summary |
|---|---|
| [close](close.md) | `fun close(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [delete](delete.md) | `fun delete(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, term: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Delete an object from the index. |
| [getAndUpdate](get-and-update.md) | `fun getAndUpdate(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Safe way to bulk update objects. Gets the object from the index first before applying the lambda to it to modify the existing object. If you set `retryConflictingUpdates` &gt; 0, it will attempt to retry to get the latest document and apply the `updateFunction` if there is a version conflict. |
| [index](--index--.md) | `fun index(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, obj: `[`T`](index.md#T)`, create: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Index an object. |
| [update](update.md) | `fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, primaryTerms: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, original: `[`T`](index.md#T)`, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)<br>Bulk update objects. If you have the object (e.g. because you are processing the sequence of a scrolling search), you can update what you have in a safe way.  If you set `retryConflictingUpdates` &gt; 0, it will retry by getting the latest version and re-applying the `updateFunction` in case of a version conflict. |
