[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](./index.md)

# IndexDAO

`class IndexDAO<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

DAO (Data Access Object) abstraction that allows you to work with indices.

You should create a DAO for each index you work with. You need to specify a [ModelReaderAndWriter](../-model-reader-and-writer/index.md) for serialization and deserialization.

### Parameters

`T` - the type of the object that is stored in the index.

`indexName` - name of the index

`indexReadAlias` - Alias used for read operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.

`indexWriteAlias` - Alias used for write operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.

`type` - the type of the documents in the index; defaults to "_doc". Since ES 6, there can only be one type. Types will be deprecated in ES 7 and removed in ES 8.

`modelReaderAndWriter` - serialization of your model class.

`refreshAllowed` - if false, the [refresh](refresh.md) will throw an exception. Defaults to false.

`defaultRequestOptions` - passed on all API calls. Defaults to [RequestOptions.DEFAULT](#). Use this to set custom headers or override on each call on the dao.

**See Also**

[RestHighLevelClient.crudDao](#)

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `IndexDAO(indexName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, client: RestHighLevelClient, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>, refreshAllowed: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "_doc", indexWriteAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = indexName, indexReadAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = indexWriteAlias, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT)`<br>DAO (Data Access Object) abstraction that allows you to work with indices. |

### Properties

| Name | Summary |
|---|---|
| [indexName](index-name.md) | `val indexName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>name of the index |
| [indexReadAlias](index-read-alias.md) | `val indexReadAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Alias used for read operations. If you are using aliases, you can separate reads and writes. Defaults to indexName. |
| [indexWriteAlias](index-write-alias.md) | `val indexWriteAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>Alias used for write operations. If you are using aliases, you can separate reads and writes. Defaults to indexName. |
| [type](type.md) | `val type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)<br>the type of the documents in the index; defaults to "_doc". Since ES 6, there can only be one type. Types will be deprecated in ES 7 and removed in ES 8. |

### Functions

| Name | Summary |
|---|---|
| [bulk](bulk.md) | `fun bulk(bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkIndexingSession.BulkOperation`](../-bulk-indexing-session/-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, operationsBlock: `[`BulkIndexingSession`](../-bulk-indexing-session/index.md)`<`[`T`](index.md#T)`>.(session: `[`BulkIndexingSession`](../-bulk-indexing-session/index.md)`<`[`T`](index.md#T)`>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [bulkIndexer](bulk-indexer.md) | `fun bulkIndexer(bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkIndexingSession.BulkOperation`](../-bulk-indexing-session/-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, requestOptions: RequestOptions = this.defaultRequestOptions): `[`BulkIndexingSession`](../-bulk-indexing-session/index.md)`<`[`T`](index.md#T)`>` |
| [createIndex](create-index.md) | `fun createIndex(requestOptions: RequestOptions = this.defaultRequestOptions, waitForActiveShards: ActiveShardCount? = null, block: CreateIndexRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [currentAliases](current-aliases.md) | `fun currentAliases(requestOptions: RequestOptions = this.defaultRequestOptions): `[`Set`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-set/index.html)`<AliasMetaData>` |
| [delete](delete.md) | `fun delete(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestOptions: RequestOptions = this.defaultRequestOptions): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [deleteIndex](delete-index.md) | `fun deleteIndex(requestOptions: RequestOptions = this.defaultRequestOptions): `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [get](get.md) | `fun get(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`T`](index.md#T)`?` |
| [getWithGetResponse](get-with-get-response.md) | `fun getWithGetResponse(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestOptions: RequestOptions = this.defaultRequestOptions): `[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<`[`T`](index.md#T)`, GetResponse>?` |
| [index](--index--.md) | `fun index(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, obj: `[`T`](index.md#T)`, create: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, primaryTerm: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, requestOptions: RequestOptions = this.defaultRequestOptions): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [refresh](refresh.md) | `fun refresh(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [search](search.md) | `fun search(scrolling: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, scrollTtlInMinutes: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 1, requestOptions: RequestOptions = this.defaultRequestOptions, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`SearchResults`](../-search-results/index.md)`<`[`T`](index.md#T)`>` |
| [searchAsync](search-async.md) | `suspend fun searchAsync(requestOptions: RequestOptions = this.defaultRequestOptions, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`SearchResults`](../-search-results/index.md)`<`[`T`](index.md#T)`>` |
| [update](update.md) | `fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, maxUpdateTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 2, requestOptions: RequestOptions = this.defaultRequestOptions, transformFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
