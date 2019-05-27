[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.client](index.md) / [search](./search.md)

# search

`fun RestHighLevelClient.search(requestOptions: RequestOptions = RequestOptions.DEFAULT, block: SearchRequest.() -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`): SearchResponse`

Search documents in the index. Expects a search block that takes a `SearchRequest` where you specify the query.
The search request already has your index. Also see extension functions added in `org.elasticsearch.action.search.SearchRequest`

