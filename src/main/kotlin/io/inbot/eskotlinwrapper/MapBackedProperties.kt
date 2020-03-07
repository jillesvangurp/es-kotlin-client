package io.inbot.eskotlinwrapper

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.writeAny

/**
 * Use this to create XContent friendly classes where you mix type safe fields with allowing the user to add
 * whatever to the underlying map. This way we don't have to map the full DSL and still get some benefits from
 * using Kotlin.
 */
@Suppress("UNCHECKED_CAST")
@MapPropertiesDSLMarker
open class MapBackedProperties internal constructor(
    internal val _properties: MutableMap<String, Any> = mutableMapOf()
) : MutableMap<String, Any> by _properties, ToXContent {

    override fun get(key: String) = _properties[key.snakeCaseToUnderscore()]
    override fun put(key: String, value: Any) {
        _properties[key.snakeCaseToUnderscore()] = value
    }

    internal fun <T : Any?> property(): ReadWriteProperty<Any, T> {
        return object :
            ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return _properties[property.name.snakeCaseToUnderscore()] as T
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                _properties[property.name.snakeCaseToUnderscore()] = value as Any // cast is needed here apparently
            }
        }
    }

    internal fun <T : Any?> property(customPropertyName: String): ReadWriteProperty<MapBackedProperties, T> {
        return object :
            ReadWriteProperty<MapBackedProperties, T> {
            override fun getValue(thisRef: MapBackedProperties, property: KProperty<*>): T {
                return _properties[customPropertyName] as T
            }

            override fun setValue(thisRef: MapBackedProperties, property: KProperty<*>, value: T) {
                _properties[customPropertyName] = value as Any // cast is needed here apparently
            }
        }
    }

    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params?): XContentBuilder {
        builder.writeAny(this)
        return builder
    }

    fun getOrCreateMutableList(key: String): MutableList<Any> {
        val list = this[key] as MutableList<Any>?
        if (list == null) {
            this[key] = mutableListOf<Any>()
        }
        return this[key] as MutableList<Any>
    }
}

fun mapProps(block: MapBackedProperties.()->Unit): MapBackedProperties {
    val mapBackedProperties = MapBackedProperties()
    block.invoke(mapBackedProperties)
    return mapBackedProperties
}
