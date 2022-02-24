package com.jillesvangurp.mapbackedproperties

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@DslMarker
annotation class MapPropertiesDSLMarker

private val re = "(?<=[a-z0-9])[A-Z]".toRegex()
fun String.snakeCaseToUnderscore(): String {
    return re.replace(this) { m -> "_${m.value}" }.lowercase()
}

enum class PropertyNamingConvention {
    AsIs,
    ConvertToSnakeCase
}

fun String.convertPropertyName(namingConvention: PropertyNamingConvention):String {
    return when(namingConvention) {
        PropertyNamingConvention.AsIs -> this // e.g. kotlin convention is camelCase
        PropertyNamingConvention.ConvertToSnakeCase -> this.snakeCaseToUnderscore()
    }
}
/**
 * Mutable Map of String to Any that normalizes the keys to use underscores. You can use this as a base class
 * for creating Kotlin DSLs for e.g. Json DSLs such as the Elasticsearch query DSL.
 */
@Suppress("UNCHECKED_CAST")
@MapPropertiesDSLMarker
open class MapBackedProperties(
    internal val namingConvention: PropertyNamingConvention = PropertyNamingConvention.AsIs,
    internal val _properties: MutableMap<String, Any> = mutableMapOf(),
) : MutableMap<String, Any> by _properties, IMapBackedProperties {


    override fun get(key: String) = _properties[key.snakeCaseToUnderscore()]

    override fun put(key: String, value: Any, namingConvention: PropertyNamingConvention) {
        _properties[key.convertPropertyName(namingConvention)] = value
    }

    override fun put(key: String, value: Any) {
        _properties[key.convertPropertyName(namingConvention)] = value
    }

    /**
     * Property delegate that stores the value in the MapBackedProperties. Use this to create type safe
     * properties.
     */
    override fun <T : Any?> property(): ReadWriteProperty<Any, T> {
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return _properties[property.name.convertPropertyName(namingConvention)] as T
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                _properties[property.name.convertPropertyName(namingConvention)] = value as Any
            }
        }
    }

    /**
     * Property delegate that stores the value in the MapBackedProperties; uses the customPropertyName instead of the
     * kotlin property name. Use this to create type safe properties in case the property name you need overlaps clashes
     * with a kotlin keyword or super class property or method. For example, "size" is also a method on
     * MapBackedProperties and thus cannot be used as a kotlin property name in a Kotlin class implementing Map.
     */
    override fun <T : Any?> property(customPropertyName: String): ReadWriteProperty<MapBackedProperties, T> {
        return object : ReadWriteProperty<MapBackedProperties, T> {
            override fun getValue(thisRef: MapBackedProperties, property: KProperty<*>): T {
                return _properties[customPropertyName] as T
            }

            override fun setValue(thisRef: MapBackedProperties, property: KProperty<*>, value: T) {
                _properties[customPropertyName] = value as Any // cast is needed here apparently
            }
        }
    }

    /**
     * Helper to manipulate list value objects.
     */
    override fun getOrCreateMutableList(key: String): MutableList<Any> {
        val list = this[key] as MutableList<Any>?
        if (list == null) {
            this[key] = mutableListOf<Any>()
        }
        return this[key] as MutableList<Any>
    }



    override fun toString(): String {
        return _properties.toString()
    }
}


