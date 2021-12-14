package com.jillesvangurp.eskotlinwrapper

import kotlinx.serialization.json.*

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