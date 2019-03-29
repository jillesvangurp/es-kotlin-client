[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [SearchResults](./index.md)

# SearchResults

`interface SearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

### Properties

| [hits](hits.md) | `open val hits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<SearchHit, `[`T`](index.md#T)`?>>` |
| [mappedHits](mapped-hits.md) | `open val mappedHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`T`](index.md#T)`>` |
| [modelReaderAndWriter](model-reader-and-writer.md) | `abstract val modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>` |
| [searchHits](search-hits.md) | `abstract val searchHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<SearchHit>` |
| [searchResponse](search-response.md) | `abstract val searchResponse: SearchResponse` |
| [totalHits](total-hits.md) | `abstract val totalHits: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html) |

### Inheritors

| [PagedSearchResults](../-paged-search-results/index.md) | `class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](./index.md)`<`[`T`](../-paged-search-results/index.md#T)`>` |
| [ScrollingSearchResults](../-scrolling-search-results/index.md) | `class ScrollingSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](./index.md)`<`[`T`](../-scrolling-search-results/index.md#T)`>` |

