package io.inbot.eskotlinwrapper

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Use this to create XContent friendly classes where you mix type safe fields with allowing the user to add
 * whatever to the underlying map. This way we don't have to map the full DSL and still get some benefits from
 * using Kotlin.
 */
@Suppress("UNCHECKED_CAST")
@MapPropertiesDSL
open class MapBackedProperties internal constructor(
    internal val _properties: MutableMap<String, Any> = mutableMapOf()
) : MutableMap<String, Any> by _properties {

    override fun get(key: String) = _properties[key.snakeCaseToUnderscore()]
    override fun put(key: String, value: Any) {
        _properties[key.snakeCaseToUnderscore()] = value
    }

    internal fun <T : Any?> property(): ReadWriteProperty<MapBackedProperties, T> {
        return object :
            ReadWriteProperty<MapBackedProperties, T> {
            override fun getValue(thisRef: MapBackedProperties, property: KProperty<*>): T {
                return _properties[property.name.snakeCaseToUnderscore()] as T
            }

            override fun setValue(thisRef: MapBackedProperties, property: KProperty<*>, value: T) {
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
}