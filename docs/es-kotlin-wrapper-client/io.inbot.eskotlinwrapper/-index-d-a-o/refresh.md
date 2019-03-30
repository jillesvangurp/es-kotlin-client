[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [refresh](./refresh.md)

# refresh

`fun refresh(): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Call the refresh API on elasticsearch. You should not use this other than in tests. E.g. when testing search queries, you often want to refresh after indexing before calling search

### Exceptions

`UnsupportedOperationException` - if you do not explicitly opt in to this by setting the `refreshAllowed parameter on the dao`.