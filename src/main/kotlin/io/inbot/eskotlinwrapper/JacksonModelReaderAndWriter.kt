package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

class JacksonModelReaderAndWriter<T : Any>(
    override val clazz: KClass<T>,
    val objectMapper: ObjectMapper
) : ModelReaderAndWriter<T> {
    override fun deserializer(): (ByteArray?) -> T = { bytes -> objectMapper.readValue(bytes, clazz.java)!! }
    override fun serializer(): (T) -> ByteArray = { value -> objectMapper.writeValueAsBytes(value) }
}