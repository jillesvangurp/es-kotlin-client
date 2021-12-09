package com.jillesvangurp.eskotlinwrapper

import com.jillesvangurp.eskotlinwrapper.dsl.ESQuery
import org.elasticsearch.xcontent.stringify
import org.elasticsearch.xcontent.writeAny
import org.elasticsearch.xcontent.ToXContent
import org.elasticsearch.xcontent.XContentBuilder
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface IMapBackedProperties : MutableMap<String, Any> {
    fun putNoSnakeCase(key: String, value: Any)
}
/**
 * Mutable Map of String to Any that normalizes the keys to use underscores. This is a key component used for
 * implementing DSLs for querying, mappings, and other things in Elasticsearch. You may also use this to extend the
 * DSL. Either extend directly or use this via e.g. interface delegation.
 *
 * Implements ToXContent so you can use this anywhere the Elasticsearch Java API expects an XContent object.
 * This works together with the xcontent extension functions this library adds.
 */
@Suppress("UNCHECKED_CAST")
@MapPropertiesDSLMarker
open class MapBackedProperties(
    internal val _properties: MutableMap<String, Any> = mutableMapOf()
) : MutableMap<String, Any> by _properties, ToXContent, IMapBackedProperties {

    override fun get(key: String) = _properties[key.snakeCaseToUnderscore()]

    override fun putNoSnakeCase(key: String, value: Any) {
        _properties[key] = value
    }

    override fun put(key: String, value: Any) {
        _properties[key.snakeCaseToUnderscore()] = value
    }

    fun esQueryProperty(): ReadWriteProperty<Any, ESQuery> {
        return object : ReadWriteProperty<Any, ESQuery> {
            override fun getValue(thisRef: Any, property: KProperty<*>): ESQuery {
                val map = _properties[property.name] as Map<String, MapBackedProperties>
                val (name, queryDetails) = map.entries.first()
                return ESQuery(name, queryDetails)
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: ESQuery) {
                _properties[property.name] = value.toMap()
            }
        }
    }

    /**
     * Property delegate that stores the value in the MapBackedProperties. Use this to create type safe
     * properties.
     */
    fun <T : Any?> property(): ReadWriteProperty<Any, T> {
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                return _properties[property.name.snakeCaseToUnderscore()] as T
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                _properties[property.name.snakeCaseToUnderscore()] = value as Any // cast is needed here apparently
            }
        }
    }

    /**
     * Property delegate that stores the value in the MapBackedProperties; uses the customPropertyName instead of the
     * kotlin property name. Use this to create type safe properties in case the property name you need overlaps clashes
     * with a kotlin keyword or super class property or method. For example, "size" is also a method on
     * MapBackedProperties and thus cannot be used as a kotlin property name in sub class.
     */
    fun <T : Any?> property(customPropertyName: String): ReadWriteProperty<MapBackedProperties, T> {
        return object : ReadWriteProperty<MapBackedProperties, T> {
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

    /**
     * Helper to manipulate list value objects.
     */
    fun getOrCreateMutableList(key: String): MutableList<Any> {
        val list = this[key] as MutableList<Any>?
        if (list == null) {
            this[key] = mutableListOf<Any>()
        }
        return this[key] as MutableList<Any>
    }

    override fun toString(): String {
        return stringify(true)
    }
}

/**
 * Helper function to construct a MapBackedProperties with some content.
 */
fun mapProps(block: MapBackedProperties.() -> Unit): MapBackedProperties {
    val mapBackedProperties = MapBackedProperties()
    block.invoke(mapBackedProperties)
    return mapBackedProperties
}
