[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [index](./--index--.md)

# index

`fun index(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, obj: `[`T`](index.md#T)`, create: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = true, seqNo: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, primaryTerm: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`? = null, requestOptions: RequestOptions = this.defaultRequestOptions): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Index a document with a given [id](--index--.md#io.inbot.eskotlinwrapper.IndexDAO$index(kotlin.String, io.inbot.eskotlinwrapper.IndexDAO.T, kotlin.Boolean, kotlin.Long, kotlin.Long, org.elasticsearch.client.RequestOptions)/id). Set [create](--index--.md#io.inbot.eskotlinwrapper.IndexDAO$index(kotlin.String, io.inbot.eskotlinwrapper.IndexDAO.T, kotlin.Boolean, kotlin.Long, kotlin.Long, org.elasticsearch.client.RequestOptions)/create) to `false` for upserts. Otherwise it fails on creating documents that already exist.

You can optionally specify [seqNo](--index--.md#io.inbot.eskotlinwrapper.IndexDAO$index(kotlin.String, io.inbot.eskotlinwrapper.IndexDAO.T, kotlin.Boolean, kotlin.Long, kotlin.Long, org.elasticsearch.client.RequestOptions)/seqNo) and [primaryTerm](--index--.md#io.inbot.eskotlinwrapper.IndexDAO$index(kotlin.String, io.inbot.eskotlinwrapper.IndexDAO.T, kotlin.Boolean, kotlin.Long, kotlin.Long, org.elasticsearch.client.RequestOptions)/primaryTerm) to implement optimistic locking. However, you should use [update](update.md) does this for you.

