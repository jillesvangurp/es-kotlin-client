package com.jillesvangurp.eskotlinwrapper

import com.jillesvangurp.eskotlinwrapper.dsl.ESQuery
import kotlinx.serialization.json.*
import org.elasticsearch.xcontent.*
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

private val re = "(?<=[a-z0-9])[A-Z]".toRegex()
fun String.snakeCaseToUnderscore(): String {
    return re.replace(this) { m -> "_${m.value}" }.lowercase(Locale.getDefault())
}

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
    internal val _properties: MutableMap<String, Any> = mutableMapOf(),
    internal val useSnakeCaseConversion: Boolean = true
) : MutableMap<String, Any> by _properties, IMapBackedProperties {

    override fun get(key: String) = _properties[key.snakeCaseToUnderscore()]

    override fun putNoSnakeCase(key: String, value: Any) {
        _properties[key] = value
    }

    override fun put(key: String, value: Any) {
        if (useSnakeCaseConversion) {
            _properties[key.snakeCaseToUnderscore()] = value
        } else {
            _properties[key] = value
        }
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
                return if(useSnakeCaseConversion) {
                    _properties[property.name.snakeCaseToUnderscore()] as T
                } else {
                    _properties[property.name] as T
                }
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                if(useSnakeCaseConversion) {
                    // cast is needed here apparently
                    _properties[property.name.snakeCaseToUnderscore()] = value as Any // cast is needed here apparently
                } else
                {
                    _properties[property.name] = value as Any // cast is needed here apparently
                }
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

fun MapBackedProperties.toXContent(builder: XContentBuilder): XContentBuilder {
    builder.writeAny(this)
    return builder
}

fun MapBackedProperties.stringify(pretty: Boolean = false): String {
    val bos = ByteArrayOutputStream()
    val builder = XContentFactory.jsonBuilder(bos)
    if (pretty) {
        builder.prettyPrint()
    }
    toXContent(builder)
    builder.close()
    bos.flush()
    bos.close()
    return bos.toByteArray().toString(StandardCharsets.UTF_8)
}

fun MapBackedProperties.asJsonObject(): JsonObject {
    val map = this
    return buildJsonObject {
        map.entries.forEach { (field, value) ->
            put(field, toJsonElement(value))
        }
    }
}
fun toJsonElement(e: Any?): JsonElement {
    return when (e) {
        e == null -> JsonNull
        is Number -> JsonPrimitive(e)
        is String -> JsonPrimitive(e)
        is Boolean -> JsonPrimitive(e)
        is List<*> -> buildJsonArray {
            e.forEach {
                this.add(toJsonElement(it))
            }
        }
        is Map<*, *> -> buildJsonObject {
            e.entries.forEach { (field, value) ->
                put(field.toString(), toJsonElement(value))
            }
        }
        else -> throw IllegalArgumentException("unhandled element type ${e!!::class.simpleName}")
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
