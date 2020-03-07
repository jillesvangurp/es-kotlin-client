package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import kotlin.reflect.KClass

/**
 * Simple implementation of [ModelReaderAndWriter] that uses a jackson object mapper.
 */
class JacksonModelReaderAndWriter<T : Any>(
    override val clazz: KClass<T>,
    val objectMapper: ObjectMapper
) : ModelReaderAndWriter<T> {

    /**
     * Alternate constructor so we can instantiate from Java as well
     */
    constructor(javaClazz: Class<T>, objectMapper: ObjectMapper) : this(javaClazz.kotlin, objectMapper)

    override fun deserializer(): (ByteArray?) -> T = { bytes ->
        objectMapper.readValue(bytes, clazz.java)!!
    }
    override fun serializer(): (T) -> ByteArray = { value -> objectMapper.writeValueAsBytes(value) }

    companion object {
        inline fun <reified T : Any> create(objectMapper: ObjectMapper? = null): JacksonModelReaderAndWriter<T> {
            return if (objectMapper == null) {
                val om = ObjectMapper().findAndRegisterModules()
                // sane default, if you want camelcase, override the objectmapper
                om.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
                JacksonModelReaderAndWriter<T>(T::class, om)
            } else {
                JacksonModelReaderAndWriter<T>(T::class, objectMapper)
            }
        }
    }
}
