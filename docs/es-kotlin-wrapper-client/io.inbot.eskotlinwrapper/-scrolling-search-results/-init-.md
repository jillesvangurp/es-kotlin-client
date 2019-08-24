[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [ScrollingSearchResults](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`ScrollingSearchResults(searchResponse: SearchResponse, modelReaderAndWriter: `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>, restHighLevelClient: RestHighLevelClient, scrollTtlInMinutes: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT)`

Represents scrolling search results. Accessing the [searchHits](search-hits.md) causes pages of results to be retrieved lazily.

Note. you can only consume the sequence once.

