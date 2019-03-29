[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkIndexingSession](index.md) / [getAndUpdate](./get-and-update.md)

# getAndUpdate

`fun getAndUpdate(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, itemCallback: (`[`BulkIndexingSession.BulkOperation`](-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = this::defaultItemResponseCallback, updateFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)