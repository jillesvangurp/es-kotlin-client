[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.client](index.md) / [clearScrollAsync](./clear-scroll-async.md)

# clearScrollAsync

`suspend fun RestHighLevelClient.clearScrollAsync(vararg scrollIds: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`, requestOptions: RequestOptions = RequestOptions.DEFAULT): ClearScrollResponse`

Clear the scroll after you are done. If you use the DAO for scrolling searches, this is called for you.

