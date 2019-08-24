[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [IndexDAO](index.md) / [search](./search.md)

# search

`fun search(scrolling: `[`Boolean`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)` = false, scrollTtlInMinutes: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)` = 1, requestOptions: RequestOptions = this.defaultRequestOptions, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): `[`SearchResults`](../-search-results/index.md)`<`[`T`](index.md#T)`>`

Perform a search against your index. This creates a `SearchRequest` that is passed into the [block](search.md#io.inbot.eskotlinwrapper.IndexDAO$search(kotlin.Boolean, kotlin.Long, org.elasticsearch.client.RequestOptions, kotlin.Function1((org.elasticsearch.action.search.SearchRequest, kotlin.Unit)))/block) so you can
customize it. Inside the [block](search.md#io.inbot.eskotlinwrapper.IndexDAO$search(kotlin.Boolean, kotlin.Long, org.elasticsearch.client.RequestOptions, kotlin.Function1((org.elasticsearch.action.search.SearchRequest, kotlin.Unit)))/block) you can manipulate the request to set a `source` and other parameters.

Returns a [SearchResults](../-search-results/index.md) instance that you can use to get the deserialized results or the raw response.

If you want to perform a scrolling search, all you have to do is set [scrolling](search.md#io.inbot.eskotlinwrapper.IndexDAO$search(kotlin.Boolean, kotlin.Long, org.elasticsearch.client.RequestOptions, kotlin.Function1((org.elasticsearch.action.search.SearchRequest, kotlin.Unit)))/scrolling) to true (default is false).
You can also set a [scrollTtlInMinutes](search.md#io.inbot.eskotlinwrapper.IndexDAO$search(kotlin.Boolean, kotlin.Long, org.elasticsearch.client.RequestOptions, kotlin.Function1((org.elasticsearch.action.search.SearchRequest, kotlin.Unit)))/scrollTtlInMinutes) if you want something else than the default of 1 minute.

