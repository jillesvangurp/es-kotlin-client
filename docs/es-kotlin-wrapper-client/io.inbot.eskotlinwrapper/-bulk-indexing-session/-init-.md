[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkIndexingSession](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`BulkIndexingSession(client: RestHighLevelClient, dao: `[`IndexDAO`](../-index-d-a-o/index.md)`<`[`T`](index.md#T)`>, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`> = dao.modelReaderAndWriter, bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, defaultRequestOptions: RequestOptions = dao.defaultRequestOptions)`

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