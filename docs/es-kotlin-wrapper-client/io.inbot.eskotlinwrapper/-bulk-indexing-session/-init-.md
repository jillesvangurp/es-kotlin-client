[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkIndexingSession](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`BulkIndexingSession(client: RestHighLevelClient, dao: `[`IndexDAO`](../-index-d-a-o/index.md)`<`[`T`](index.md#T)`>, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`> = dao.modelReaderAndWriter, bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, defaultRequestOptions: RequestOptions = dao.defaultRequestOptions)`

Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](../-index-d-a-o/index.md).

```
dao.bulk() {
  index("xxx",myObject)
}
```

