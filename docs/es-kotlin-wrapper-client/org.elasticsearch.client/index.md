[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.client](./index.md)

## Package org.elasticsearch.client

### Types

| Name | Summary |
|---|---|
| [SuspendingActionListener](-suspending-action-listener/index.md) | `class SuspendingActionListener<T> : ActionListener<`[`T`](-suspending-action-listener/index.md#T)`>` |

### Functions

| Name | Summary |
|---|---|
| [createIndexAsync](create-index-async.md) | `suspend fun IndicesClient.createIndexAsync(index: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestOptions: RequestOptions = RequestOptions.DEFAULT, block: CreateIndexRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): CreateIndexResponse` |
| [crudDao](crud-dao.md) | `fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RestHighLevelClient.crudDao(index: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, modelReaderAndWriter: `[`ModelReaderAndWriter`](../io.inbot.eskotlinwrapper/-model-reader-and-writer/index.md)`<`[`T`](crud-dao.md#T)`>, type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "_doc", readAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = index, writeAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = index, refreshAllowed: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT): `[`IndexDAO`](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md)`<`[`T`](crud-dao.md#T)`>`<br>`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RestHighLevelClient.crudDao(index: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules(), refreshAllowed: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false): `[`IndexDAO`](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md)`<`[`T`](crud-dao.md#T)`>` |
| [RestHighLevelClient](-rest-high-level-client.md) | `fun RestHighLevelClient(host: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "localhost", port: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 9200, https: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, user: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, password: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): RestHighLevelClient`<br>Fake constructor like factory that gives you sane defaults that will allow you to quickly connect to elastic cloud. |
| [search](search.md) | `fun RestHighLevelClient.search(requestOptions: RequestOptions = RequestOptions.DEFAULT, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): SearchResponse` |
| [searchAsync](search-async.md) | `suspend fun RestHighLevelClient.searchAsync(requestOptions: RequestOptions = RequestOptions.DEFAULT, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): SearchResponse` |
