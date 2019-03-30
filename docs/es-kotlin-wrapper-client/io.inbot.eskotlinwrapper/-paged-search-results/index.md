[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [PagedSearchResults](./index.md)

# PagedSearchResults

`class PagedSearchResults<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`SearchResults`](../-search-results/index.md)`<`[`T`](index.md#T)`>`

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `PagedSearchResults(searchResponse: SearchResponse, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>)` |

### Properties

| Name | Summary |
|---|---|
| [modelReaderAndWriter](model-reader-and-writer.md) | `val modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>`<br>the model reader and writer used for deserialization. |
| [searchHits](search-hits.md) | `val searchHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<SearchHit>`<br>search hits as a sequence or an empty sequence. |
| [searchResponse](search-response.md) | `val searchResponse: SearchResponse`<br>the original search response. |
| [totalHits](total-hits.md) | `val totalHits: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)<br>The total number of hits for the query. |

### Inherited Properties

| Name | Summary |
|---|---|
| [hits](../-search-results/hits.md) | `open val hits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`Pair`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)`<SearchHit, `[`T`](../-search-results/index.md#T)`?>>`<br>a sequence of pairs of search hits and their deserialized objects. |
| [mappedHits](../-search-results/mapped-hits.md) | `open val mappedHits: `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`T`](../-search-results/index.md#T)`>`<br>deserialized objects inside the `searchHits`. Deserialization happens lazily. |
