[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [SuspendingActionListener](index.md) / [suspending](./suspending.md)

# suspending

`suspend fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> suspending(block: (`[`SuspendingActionListener`](index.md)`<`[`T`](suspending.md#T)`>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`T`](suspending.md#T)

Use this to call java async methods in a co-routine.

``` kotlin
suspending {
  this.searchAsync(searchRequest, requestOptions, it)
}
```

