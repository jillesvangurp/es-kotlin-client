[es-kotlin-wrapper-client](../index.md) / [io.inbot.eskotlinwrapper](./index.md)

## Package io.inbot.eskotlinwrapper

Misc. classes that support the various extension functions on the client.

### Types

| Name | Summary |
|---|---|
| [AsyncBulkIndexingSession](-async-bulk-indexing-session/index.md) | `class AsyncBulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>Asynchronous bulk indexing that uses the experimental Kotlin flows. Works similar to the synchronous version except it fires bulk requests asynchronously on the specified dispatcher. On paper using multiple threads, allows ES to use multiple Threads to consume bulk requests. |
| [BulkIndexingSession](-bulk-indexing-session/index.md) | `class BulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)<br>Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](-index-d-a-o/index.md). Implements `AutoCloseable` to ensure all operations are processed. |
| [BulkOperation](-bulk-operation/index.md) | `data class BulkOperation<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>Bulk operation model used for e.g. `itemCallback`. |
| [IndexDAO](-index-d-a-o/index.md) | `class IndexDAO<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>DAO (Data Access Object) abstraction that allows you to work with indices. |
| [JacksonModelReaderAndWriter](-jackson-model-reader-and-writer/index.md) | `class JacksonModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`ModelReaderAndWriter`](-model-reader-and-writer/index.md)`<`[`T`](-jackson-model-reader-and-writer/index.md#T)`>`<br>Simple implementation of [ModelReaderAndWriter](-model-reader-and-writer/index.md) that uses a jackson object mapper. |
| [ModelReaderAndWriter](-model-reader-and-writer/index.md) | `interface ModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>Implement this for custom serialization/deserialization of objects in your index. Use this in combination with a [IndexDAO](-index-d-a-o/index.md). |
| [PagedSearchResults](-paged-search-results/index.md) | `class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-paged-search-results/index.md#T)`>` |
| [ScrollingSearchResults](-scrolling-search-results/index.md) | `class ScrollingSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-scrolling-search-results/index.md#T)`>` |
| [SearchResults](-search-results/index.md) | `interface SearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>Abstraction for search results that applies to both scrolling and non scrolling searches. |
| [SuspendingActionListener](-suspending-action-listener/index.md) | `class SuspendingActionListener<T> : ActionListener<`[`T`](-suspending-action-listener/index.md#T)`>`<br>Action listener that can be used with to adapt the async methods across the java client to co-routines. |
