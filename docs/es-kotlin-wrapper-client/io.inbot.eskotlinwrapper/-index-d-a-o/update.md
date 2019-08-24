[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [update](./update.md)

# update

`fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, maxUpdateTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 2, requestOptions: RequestOptions = this.defaultRequestOptions, transformFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Updates document identified by [id](update.md#io.inbot.eskotlinwrapper.IndexDAO$update(kotlin.String, kotlin.Int, org.elasticsearch.client.RequestOptions, kotlin.Function1((io.inbot.eskotlinwrapper.IndexDAO.T, )))/id) by fetching the current version with [get](get.md) and then applying the [transformFunction](update.md#io.inbot.eskotlinwrapper.IndexDAO$update(kotlin.String, kotlin.Int, org.elasticsearch.client.RequestOptions, kotlin.Function1((io.inbot.eskotlinwrapper.IndexDAO.T, )))/transformFunction) to produce the updated version.

if [maxUpdateTries](update.md#io.inbot.eskotlinwrapper.IndexDAO$update(kotlin.String, kotlin.Int, org.elasticsearch.client.RequestOptions, kotlin.Function1((io.inbot.eskotlinwrapper.IndexDAO.T, )))/maxUpdateTries) &gt; 0, it will deal with version conflicts (e.g. due to concurrent updates) by retrying with the latest version.

