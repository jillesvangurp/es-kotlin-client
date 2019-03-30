[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [update](./update.md)

# update

`fun update(id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, maxUpdateTries: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 2, requestOptions: RequestOptions = this.defaultRequestOptions, transformFunction: (`[`T`](index.md#T)`) -> `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Update document by fetching the current version with `get` and then applying the `transformFunction` to produce the updated version.

### Parameters

`maxUpdateTries` - if &gt; 0, it will deal with version conflicts (e.g. due to concurrent updates) by retrying with the latest version.