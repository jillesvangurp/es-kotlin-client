[es-kotlin-wrapper-client](../index.md) / [io.inbot.eskotlinwrapper](./index.md)

## Package io.inbot.eskotlinwrapper

### Types

| Name | Summary |
|---|---|
| [BulkIndexingSession](-bulk-indexing-session/index.md) | `class BulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html)<br>Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](-index-d-a-o/index.md). |
| [IndexDAO](-index-d-a-o/index.md) | `class IndexDAO<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`<br>DAO (Data Access Object) abstraction that allows you to work with indices. |
| [JacksonModelReaderAndWriter](-jackson-model-reader-and-writer/index.md) | `class JacksonModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`ModelReaderAndWriter`](-model-reader-and-writer/index.md)`<`[`T`](-jackson-model-reader-and-writer/index.md#T)`>` |
| [ModelReaderAndWriter](-model-reader-and-writer/index.md) | `interface ModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [PagedSearchResults](-paged-search-results/index.md) | `class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-paged-search-results/index.md#T)`>` |
| [ScrollingSearchResults](-scrolling-search-results/index.md) | `class ScrollingSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-scrolling-search-results/index.md#T)`>` |
| [SearchResults](-search-results/index.md) | `interface SearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [SuspendingActionListener](-suspending-action-listener/index.md) | `class SuspendingActionListener<T> : ActionListener<`[`T`](-suspending-action-listener/index.md#T)`>`<br>Action listener that can be used with to adapt the async methods across the java client to co-routines. |
