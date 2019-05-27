[es-kotlin-wrapper-client](../index.md) / [org.elasticsearch.search](index.md) / [mapHits](./map-hits.md)

# mapHits

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> SearchHits.mapHits(fn: (SearchHit) -> `[`T`](map-hits.md#T)`): `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`T`](map-hits.md#T)`>`

Shortcut to hits.map.

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> SearchHits.mapHits(modelReaderAndWriter: `[`ModelReaderAndWriter`](../io.inbot.eskotlinwrapper/-model-reader-and-writer/index.md)`<`[`T`](map-hits.md#T)`>): `[`Sequence`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.sequences/-sequence/index.html)`<`[`T`](map-hits.md#T)`>`

Deserialize the results as a sequence.

