[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [SearchResults](./index.md)

# SearchResults

`interface SearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

Abstraction for search results that applies to both scrolling and non scrolling searches.

### Properties

| Name | Summary |
|---|---|
| [hits](hits.md) | `open val hits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<SearchHit, `[`T`](index.md#T)`?>>`<br>a sequence of pairs of search hits and their deserialized objects. |
| [mappedHits](mapped-hits.md) | `open val mappedHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`T`](index.md#T)`>`<br>deserialized objects inside the `searchHits`. Deserialization happens lazily. |
| [modelReaderAndWriter](model-reader-and-writer.md) | `abstract val modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>`<br>the model reader and writer used for deserialization. |
| [searchHits](search-hits.md) | `abstract val searchHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<SearchHit>`<br>search hits as a sequence or an empty sequence. |
| [searchResponse](search-response.md) | `abstract val searchResponse: SearchResponse`<br>the original search response. |
| [totalHits](total-hits.md) | `abstract val totalHits: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>The total number of hits for the query. |

### Inheritors

| Name | Summary |
|---|---|
| [PagedSearchResults](../-paged-search-results/index.md) | `class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](./index.md)`<`[`T`](../-paged-search-results/index.md#T)`>` |
| [ScrollingSearchResults](../-scrolling-search-results/index.md) | `class ScrollingSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](./index.md)`<`[`T`](../-scrolling-search-results/index.md#T)`>` |
