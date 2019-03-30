[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [SuspendingActionListener](./index.md)

# SuspendingActionListener

`class SuspendingActionListener<T> : ActionListener<`[`T`](index.md#T)`>`

Action listener that can be used with to adapt the async methods across the java client to co-routines.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SuspendingActionListener(continuation: `[`Continuation`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-continuation/index.html)`<`[`T`](index.md#T)`>)`<br>Action listener that can be used with to adapt the async methods across the java client to co-routines. |

### Functions

| Name | Summary |
|---|---|
| [onFailure](on-failure.md) | `fun onFailure(e: `[`Exception`](https://docs.oracle.com/javase/8/docs/api/java/lang/Exception.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onResponse](on-response.md) | `fun onResponse(response: `[`T`](index.md#T)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |

### Companion Object Functions

| Name | Summary |
|---|---|
| [suspending](suspending.md) | `suspend fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> suspending(block: (`[`SuspendingActionListener`](./index.md)`<`[`T`](suspending.md#T)`>) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`T`](suspending.md#T)<br>Use this to call java async methods in a co-routine. |
