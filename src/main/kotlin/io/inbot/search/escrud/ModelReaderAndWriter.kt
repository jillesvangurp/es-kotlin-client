package io.inbot.search.escrud

import org.elasticsearch.search.SearchHit
import kotlin.reflect.KClass

interface ModelReaderAndWriter<T : Any> {
    val clazz: KClass<T>
    fun deserializer(): (ByteArray) -> T
    fun serializer(): (T) -> ByteArray

    fun serialize(obj: T): ByteArray {
        return serializer().invoke(obj)
    }

    fun deserialize(bytes: ByteArray): T {
        return deserializer().invoke(bytes)
    }

    fun deserialize(searchHit: SearchHit): T? {
        // will throw npe if there is no source; source can be null on the java side
        val bytes = searchHit.sourceRef?.toBytesRef()?.bytes

        return if(bytes != null) deserializer().invoke(bytes) else null
    }
}
