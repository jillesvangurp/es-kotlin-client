[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.client](index.md) / [scroll](./scroll.md)

# scroll

`fun RestHighLevelClient.scroll(scrollId: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, ttl: `[`Long`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html)`, requestOptions: RequestOptions = RequestOptions.DEFAULT): SearchResponse`

Get the next page of a scrolling search. Note, use the DAO to do scrolling searches and avoid manually doing these requests.

