package io.inbot.eskotlinwrapper

import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.obj
import org.elasticsearch.common.xcontent.objField
import org.elasticsearch.common.xcontent.stringify
import org.elasticsearch.common.xcontent.writeAny
import org.elasticsearch.common.xcontent.xContentBuilder
import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Base64
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun String.snakeCaseToUnderscore(): String {
    val re = "(?<=[a-z])[A-Z]".toRegex()
    return re.replace(this) { m -> "_${m.value}" }.toLowerCase()
}

@DslMarker
annotation class MapPropertiesDSL

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
        return object : ReadWriteProperty<MapBackedProperties, T> {
            override fun getValue(thisRef: MapBackedProperties, property: KProperty<*>): T {
                return _properties[property.name.snakeCaseToUnderscore()] as T
            }

            override fun setValue(thisRef: MapBackedProperties, property: KProperty<*>, value: T) {
                _properties[property.name.snakeCaseToUnderscore()] = value as Any // cast is needed here apparently
            }
        }
    }

    internal fun <T : Any?> property(customPropertyName: String): ReadWriteProperty<MapBackedProperties, T> {
        return object : ReadWriteProperty<MapBackedProperties, T> {
            override fun getValue(thisRef: MapBackedProperties, property: KProperty<*>): T {
                return _properties[customPropertyName] as T
            }

            override fun setValue(thisRef: MapBackedProperties, property: KProperty<*>, value: T) {
                _properties[customPropertyName] = value as Any // cast is needed here apparently
            }
        }
    }
}

@MapPropertiesDSL
class IndexSettings : MapBackedProperties() {
    var replicas: Int by property("index.number_of_replicas")
    var shards: Int by property("index.number_of_shards")
}

@MapPropertiesDSL
class FieldMapping(typeName: String) : MapBackedProperties() {
    var type: String by property()
    //    var properties: Map<String, FieldMapping>? by property()
    var copyTo: List<String> by property()

    init {
        type = typeName
    }

    fun fields(block: FieldMappings.() -> Unit) {
        val fields = this["fields"] as FieldMappings? ?: FieldMappings()
        block.invoke(fields)
        this["fields"] = fields
    }
}

@MapPropertiesDSL
class FieldMappings : MapBackedProperties() {
    fun text(name: String) = field(name, "text") {}
    fun text(name: String, block: FieldMapping.() -> Unit) = field(name, "text", block)
    fun keyword(name: String) = field(name, "keyword") {}
    fun keyword(name: String, block: FieldMapping.() -> Unit) = field(name, "keyword", block)
    fun bool(name: String) = field(name, "boolean") {}
    fun bool(name: String, block: FieldMapping.() -> Unit) = field(name, "boolean", block)


    inline fun <reified T : Number> number(name: String, noinline block: FieldMapping.() -> Unit) {
        val type = when (T::class) {
            Long::class -> "long"
            Int::class -> "integer"
            Float::class -> "float"
            Double::class -> "double"
            else -> throw IllegalArgumentException("unsupported type ${T::class} explicitly specify type")
        }
        field(name, type, block)
    }

    fun objField(name:String, block: FieldMappings.() -> Unit) {
        field(name, "object") {
            val fieldMappings = FieldMappings()
            block.invoke(fieldMappings)
            if(fieldMappings.size > 0) {
                this["properties"] = fieldMappings
            }
        }
    }

    fun nestedField(name:String, block: FieldMappings.() -> Unit) {
        field(name, "nested") {
            val fieldMappings = FieldMappings()
            block.invoke(fieldMappings)
            if(fieldMappings.size > 0) {
                this["properties"] = fieldMappings
            }
        }
    }

    fun field(name: String, type: String) = field(name,type) {}

    fun field(name: String, type: String, block: FieldMapping.() -> Unit) {
        val mapping = FieldMapping(type)
        block.invoke(mapping)
        put(name, mapping)
    }

    fun stringify(pretty: Boolean = false) {
        xContentBuilder {
            if(pretty) {
                this.prettyPrint()
            }
            writeAny(_properties)
        }
    }
}

data class IndexSettingsAndMappings private constructor(private val generateMetaFields: Boolean) {
    private var settings: IndexSettings? = null
    private var meta: MapBackedProperties? = null
    private var mappings: FieldMappings? = null

    fun settings(block: IndexSettings.() -> Unit) {
        val settingsMap = IndexSettings()
        block.invoke(settingsMap)

        settings = settingsMap
    }

    fun meta(block: MapBackedProperties.() -> Unit) {
        if(meta == null) meta = MapBackedProperties()
        block.invoke(meta!!)
    }

    fun mappings(block: FieldMappings.() -> Unit) {
        if(mappings == null) mappings = FieldMappings()
        block.invoke(mappings!!)
    }

    internal fun build(pretty: Boolean = false): XContentBuilder {
        if(generateMetaFields) {
            // if it did not exist, create it.
            if(meta == null) meta = object : MapBackedProperties() {}
            val mappingJson = xContentBuilder { writeAny(mappings) }.stringify()
            meta!!["content_hash"] = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(mappingJson.toByteArray()))
            meta!!["timestamp"] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        }

        return xContentBuilder {
            if(pretty) prettyPrint()
            obj {
                if(settings != null) {
                    field("settings")
                    writeAny(settings)
                }
                if(mappings != null || meta != null) {
                    objField("mappings") {
                        if (meta != null) {
                            field("_meta")
                            writeAny(meta)
                        }
                        if (mappings != null) {
                            field("properties")
                            writeAny(mappings)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun indexSettingsAndMappings(
            generateMetaFields: Boolean = true,
            pretty: Boolean = false,
            block: IndexSettingsAndMappings.() -> Unit
        ): XContentBuilder {
            val settingsAndMappings = IndexSettingsAndMappings(generateMetaFields)
            block.invoke(settingsAndMappings)
            return settingsAndMappings.build(pretty)
        }
    }
}