[es-kotlin-wrapper-client](../index.md) / [io.inbot.eskotlinwrapper](./index.md)

## Package io.inbot.eskotlinwrapper

### Types

| [BulkIndexingSession](-bulk-indexing-session/index.md) | `class BulkIndexingSession<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`AutoCloseable`](https://docs.oracle.com/javase/8/docs/api/java/lang/AutoCloseable.html) |
| [IndexDAO](-index-d-a-o/index.md) | `class IndexDAO<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [JacksonModelReaderAndWriter](-jackson-model-reader-and-writer/index.md) | `class JacksonModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`ModelReaderAndWriter`](-model-reader-and-writer/index.md)`<`[`T`](-jackson-model-reader-and-writer/index.md#T)`>` |
| [ModelReaderAndWriter](-model-reader-and-writer/index.md) | `interface ModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [PagedSearchResults](-paged-search-results/index.md) | `class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-paged-search-results/index.md#T)`>` |
| [ScrollingSearchResults](-scrolling-search-results/index.md) | `class ScrollingSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](-search-results/index.md)`<`[`T`](-scrolling-search-results/index.md#T)`>` |
| [SearchResults](-search-results/index.md) | `interface SearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>` |
| [SuspendingActionListener](-suspending-action-listener/index.md) | `class SuspendingActionListener<T> : ActionListener<`[`T`](-suspending-action-listener/index.md#T)`>` |

### Extensions for External Classes

| [org.elasticsearch.action.search.SearchRequest](org.elasticsearch.action.search.-search-request/index.md) |  |
| [org.elasticsearch.client.RestHighLevelClient](org.elasticsearch.client.-rest-high-level-client/index.md) |  |
| [org.elasticsearch.common.xcontent.ToXContent](org.elasticsearch.common.xcontent.-to-x-content/index.md) |  |
| [org.elasticsearch.search.SearchHits](org.elasticsearch.search.-search-hits/index.md) |  |

### Functions

| [RestHighLevelClient](-rest-high-level-client.md) | `fun RestHighLevelClient(host: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)` = "localhost", port: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)` = 9200, https: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, user: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, password: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null): RestHighLevelClient`<br>Fake constructor like factory that gives you sane defaults that will allow you to quickly connect to elastic cloud. |

