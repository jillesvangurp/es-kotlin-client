[es-kotlin-wrapper-client](../../../index.md) / [io.inbot.eskotlinwrapper](../../index.md) / [BulkIndexingSession](../index.md) / [BulkOperation](./index.md)

# BulkOperation

`data class BulkOperation<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

### Constructors

| [&lt;init&gt;](-init-.md) | `BulkOperation(operation: DocWriteRequest<*>, id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, updateFunction: ((`[`T`](index.md#T)`) -> `[`T`](index.md#T)`)? = null, itemCallback: (`[`BulkIndexingSession.BulkOperation`](./index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = { _, _ -> })` |

### Properties

| [id](id.md) | `val id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [itemCallback](item-callback.md) | `val itemCallback: (`[`BulkIndexingSession.BulkOperation`](./index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [operation](operation.md) | `val operation: DocWriteRequest<*>` |
| [updateFunction](update-function.md) | `val updateFunction: ((`[`T`](index.md#T)`) -> `[`T`](index.md#T)`)?` |

