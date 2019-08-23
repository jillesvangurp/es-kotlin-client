[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [BulkOperation](./index.md)

# BulkOperation

`data class BulkOperation<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

Bulk operation model used for e.g. `itemCallback`.

### Parameters

`T` - the type of the objects in the dao.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `BulkOperation(operation: DocWriteRequest<*>, id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, updateFunction: ((`[`T`](index.md#T)`) -> `[`T`](index.md#T)`)? = null, itemCallback: (`[`BulkOperation`](./index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)` = { _, _ -> })`<br>Bulk operation model used for e.g. `itemCallback`. |

### Properties

| Name | Summary |
|---|---|
| [id](id.md) | `val id: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html) |
| [itemCallback](item-callback.md) | `val itemCallback: (`[`BulkOperation`](./index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [operation](operation.md) | `val operation: DocWriteRequest<*>` |
| [updateFunction](update-function.md) | `val updateFunction: ((`[`T`](index.md#T)`) -> `[`T`](index.md#T)`)?` |
