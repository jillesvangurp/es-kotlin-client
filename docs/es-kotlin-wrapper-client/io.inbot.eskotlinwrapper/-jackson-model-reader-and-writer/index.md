[es-kotlin-wrapper-client](../../index.md) / [io.inbot.eskotlinwrapper](../index.md) / [JacksonModelReaderAndWriter](./index.md)

# JacksonModelReaderAndWriter

`class JacksonModelReaderAndWriter<T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> : `[`ModelReaderAndWriter`](../-model-reader-and-writer/index.md)`<`[`T`](index.md#T)`>`

Simple implementation of [ModelReaderAndWriter](../-model-reader-and-writer/index.md) that uses a jackson object mapper.

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `JacksonModelReaderAndWriter(javaClazz: `[`Class`](https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html)`<`[`T`](index.md#T)`>, objectMapper: ObjectMapper)`<br>Alternate constructor so we can instantiate from Java as well`JacksonModelReaderAndWriter(clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](index.md#T)`>, objectMapper: ObjectMapper)`<br>Simple implementation of [ModelReaderAndWriter](../-model-reader-and-writer/index.md) that uses a jackson object mapper. |

### Properties

| Name | Summary |
|---|---|
| [clazz](clazz.md) | `val clazz: `[`KClass`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)`<`[`T`](index.md#T)`>` |
| [objectMapper](object-mapper.md) | `val objectMapper: ObjectMapper` |

### Functions

| Name | Summary |
|---|---|
| [deserializer](deserializer.md) | `fun deserializer(): (`[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)`?) -> `[`T`](index.md#T) |
| [serializer](serializer.md) | `fun serializer(): (`[`T`](index.md#T)`) -> `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html) |

### Inherited Functions

| Name | Summary |
|---|---|
| [deserialize](../-model-reader-and-writer/deserialize.md) | `open fun deserialize(bytes: `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html)`): `[`T`](../-model-reader-and-writer/index.md#T)`open fun deserialize(searchHit: SearchHit): `[`T`](../-model-reader-and-writer/index.md#T)<br>deserialize a searchHit. The default implementation accesses the `sourceRef` inside the searchHit. |
| [serialize](../-model-reader-and-writer/serialize.md) | `open fun serialize(obj: `[`T`](../-model-reader-and-writer/index.md#T)`): `[`ByteArray`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte-array/index.html) |
