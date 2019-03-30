[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [ModelReaderAndWriter](./index.md)

# ModelReaderAndWriter

`interface ModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`>`

Implement this for custom serialization/deserialization of objects in your index. Use this in combination with a [IndexDAO](../-index-d-a-o/index.md).

### Properties

| Name | Summary |
|---|---|
| [clazz](clazz.md) | `abstract val clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](index.md#T)`>` |

### Functions

| Name | Summary |
|---|---|
| [deserialize](deserialize.md) | `open fun deserialize(bytes: `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)`): `[`T`](index.md#T)`open fun deserialize(searchHit: SearchHit): `[`T`](index.md#T)<br>deserialize a searchHit. The default implementation accesses the `sourceRef` inside the searchHit. |
| [deserializer](deserializer.md) | `abstract fun deserializer(): (`[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)`?) -> `[`T`](index.md#T) |
| [serialize](serialize.md) | `open fun serialize(obj: `[`T`](index.md#T)`): `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html) |
| [serializer](serializer.md) | `abstract fun serializer(): (`[`T`](index.md#T)`) -> `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html) |

### Inheritors

| Name | Summary |
|---|---|
| [JacksonModelReaderAndWriter](../-jackson-model-reader-and-writer/index.md) | `class JacksonModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`ModelReaderAndWriter`](./index.md)`<`[`T`](../-jackson-model-reader-and-writer/index.md#T)`>`<br>Simple implementation of [ModelReaderAndWriter](./index.md) that uses a jackson object mapper. |
