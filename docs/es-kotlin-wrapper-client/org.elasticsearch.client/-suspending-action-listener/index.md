[es-kotlin-wrapper-client](../../index.md) / [org.elasticsearch.client](../index.md) / [SuspendingActionListener](./index.md)

# SuspendingActionListener

`class SuspendingActionListener<T> : ActionListener<`[`T`](index.md#T)`>`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SuspendingActionListener(continuation: `[`Continuation`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-continuation/index.html)`<`[`T`](index.md#T)`>)` |

### Functions

| Name | Summary |
|---|---|
| [onFailure](on-failure.md) | `fun onFailure(e: `[`Exception`](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onResponse](on-response.md) | `fun onResponse(response: `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
