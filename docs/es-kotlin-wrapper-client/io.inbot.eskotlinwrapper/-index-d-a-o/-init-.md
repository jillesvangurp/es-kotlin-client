[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`IndexDAO(indexName: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, client: RestHighLevelClient, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>, refreshAllowed: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, indexWriteAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = indexName, indexReadAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = indexWriteAlias, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT)`

DAO (Data Access Object) abstraction that allows you to work with indices.

You should create a DAO for each index you work with. You need to specify a [ModelReaderAndWriter](../-model-reader-and-writer/index.md) for serialization and deserialization.

### Parameters

`T` - the type of the object that is stored in the index.

`indexName` - name of the index

`indexReadAlias` - Alias used for read operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.

`indexWriteAlias` - Alias used for write operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.

`type` - the type of the documents in the index; defaults to null. Since ES 6, there can only be one type. Types are deprecated in ES 7 and removed in ES 8.

`modelReaderAndWriter` - serialization of your model class.

`refreshAllowed` - if false, the refresh will throw an exception. Defaults to false.

`defaultRequestOptions` - passed on all API calls. Defaults to `RequestOptions.DEFAULT`. Use this to set custom headers or override on each call on the dao.