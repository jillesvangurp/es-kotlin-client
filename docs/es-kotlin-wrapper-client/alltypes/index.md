

A kotlin client that adapts the Java HighLevel REST client from Elasticsearch. Note, much of this library consists of extension functions for classes in the `org.elasticsearch` hierarchy.

### All Types

| Name | Summary |
|---|---|
| [io.inbot.eskotlinwrapper.AsyncBulkIndexingSession](../io.inbot.eskotlinwrapper/-async-bulk-indexing-session/index.md) | Asynchronous bulk indexing that uses the experimental Kotlin flows. Works similar to the synchronous version except it fires bulk requests asynchronously on the specified dispatcher. On paper using multiple threads, allows ES to use multiple Threads to consume bulk requests. |
| [io.inbot.eskotlinwrapper.BulkIndexingSession](../io.inbot.eskotlinwrapper/-bulk-indexing-session/index.md) | Makes using bulk request easier. You can use this directly but you probably want to use it via [IndexDAO](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md). Implements `AutoCloseable` to ensure all operations are processed. |
| [io.inbot.eskotlinwrapper.BulkOperation](../io.inbot.eskotlinwrapper/-bulk-operation/index.md) | Bulk operation model used for e.g. `itemCallback`. |
| [io.inbot.eskotlinwrapper.IndexDAO](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md) | DAO (Data Access Object) abstraction that allows you to work with indices. |
| [io.inbot.eskotlinwrapper.JacksonModelReaderAndWriter](../io.inbot.eskotlinwrapper/-jackson-model-reader-and-writer/index.md) | Simple implementation of [ModelReaderAndWriter](../io.inbot.eskotlinwrapper/-model-reader-and-writer/index.md) that uses a jackson object mapper. |
| [io.inbot.eskotlinwrapper.ModelReaderAndWriter](../io.inbot.eskotlinwrapper/-model-reader-and-writer/index.md) | Implement this for custom serialization/deserialization of objects in your index. Use this in combination with a [IndexDAO](../io.inbot.eskotlinwrapper/-index-d-a-o/index.md). |
| [io.inbot.eskotlinwrapper.PagedSearchResults](../io.inbot.eskotlinwrapper/-paged-search-results/index.md) | Represents a page of search results. Returned for non scrolling searches. |
| [io.inbot.eskotlinwrapper.ScrollingSearchResults](../io.inbot.eskotlinwrapper/-scrolling-search-results/index.md) | Represents scrolling search results. Accessing the [searchHits](../io.inbot.eskotlinwrapper/-scrolling-search-results/search-hits.md) causes pages of results to be retrieved lazily. |
| [io.inbot.eskotlinwrapper.SearchResults](../io.inbot.eskotlinwrapper/-search-results/index.md) | Abstraction for search results that applies to both scrolling and non scrolling searches. |
| [io.inbot.eskotlinwrapper.SuspendingActionListener](../io.inbot.eskotlinwrapper/-suspending-action-listener/index.md) | Action listener that can be used with to adapt the async methods across the java client to co-routines. |
