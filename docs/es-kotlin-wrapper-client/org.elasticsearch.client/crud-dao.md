[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.client](index.md) / [crudDao](./crud-dao.md)

# crudDao

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> RestHighLevelClient.crudDao(index: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, modelReaderAndWriter: `[`ModelReaderAndWriter`](../io.inbot.eskotlinwrapper/-model-reader-and-writer/index.md)`<`[`T`](crud-dao.md#T)`>, type: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "_doc", readAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = index, writeAlias: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = index, refreshAllowed: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT): `[`IndexDAO`](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md)`<`[`T`](crud-dao.md#T)`>`

Create a new Data Access Object (DAO), aka. repository class. If you've used J2EE style frameworks, you should be familiar with this pattern.

This abstracts the business of telling the client which index to run against and serializing/deserializing documents in it.

