[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [ModelReaderAndWriter](index.md) / [deserialize](./deserialize.md)

# deserialize

`open fun deserialize(bytes: `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)`): `[`T`](index.md#T)

**Return**
deserialize `obj`

`open fun deserialize(searchHit: SearchHit): `[`T`](index.md#T)

deserialize a searchHit. The default implementation accesses the `sourceRef` inside the searchHit.

