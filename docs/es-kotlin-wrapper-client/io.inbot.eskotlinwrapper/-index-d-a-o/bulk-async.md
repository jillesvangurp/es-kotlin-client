[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [bulkAsync](./bulk-async.md)

# bulkAsync

`@ObsoleteCoroutinesApi @ExperimentalCoroutinesApi suspend fun bulkAsync(bulkSize: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 100, retryConflictingUpdates: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 0, refreshPolicy: RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL, itemCallback: ((`[`BulkOperation`](../-bulk-operation/index.md)`<`[`T`](index.md#T)`>, BulkItemResponse) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)? = null, bulkDispatcher: CoroutineDispatcher = Dispatchers.IO, operationsBlock: `[`AsyncBulkIndexingSession`](../-async-bulk-indexing-session/index.md)`<`[`T`](index.md#T)`>.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)

Asynchronous version of bulk indexing that uses Co-routines, Flow, and Channel internally. Some of this is
still labeled as experimental in Kotlin and may change. Using this will require setting the `kotlin.Experimental`
flag in your build and also at runtime.

This works very similar to [bulk](bulk.md) however it uses an [AsyncBulkIndexingSession](../-async-bulk-indexing-session/index.md) instead of the
[BulkIndexingSession](../-bulk-indexing-session/index.md). You can control which bulkDispatcher is used for sending asynchronous bulk requests. The
default for this is `Dispatchers.IO`. Currently, this does not run requests in parallel. However, we are
exploring options for this as this could potentially speed up indexing.

