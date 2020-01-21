package org.elasticsearch.common.xcontent

import org.elasticsearch.common.xcontent.json.JsonXContent
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

/**
 * Helper to stringify to json.
 *
 * @param pretty pretty print, defaults to false
 */
fun ToXContent.stringify(pretty: Boolean = false): String {
    val bos = ByteArrayOutputStream()
    val builder = XContentFactory.jsonBuilder(bos)
    if (pretty) {
        builder.prettyPrint()
    }
    toXContent(builder, ToXContent.EMPTY_PARAMS)
    builder.close()
    bos.flush()
    bos.close()
    return bos.toByteArray().toString(StandardCharsets.UTF_8)
}

/**
 * Helper to write json to an output stream.
 *
 * @param pretty pretty print, defaults to false
 */
fun ToXContent.stringify(out: OutputStream, pretty: Boolean = false) {
    val builder = XContentFactory.jsonBuilder(out)
    if (pretty) {
        builder.prettyPrint()
    }
    toXContent(builder, ToXContent.EMPTY_PARAMS)
    builder.close()
}

fun XContentBuilder.obj(block: XContentBuilder.() -> Unit) {
    this.startObject()
    block.invoke(this)
    this.endObject()
}

fun XContentBuilder.writeAny(obj: Any?) {
    if(obj == null) {
        this.nullValue()
    } else when(obj) {
        is Number -> {
            this.value(obj)
        }
        is String -> {
            this.value(obj)
        }
        is Boolean -> {
            this.value(obj)
        }
        is Map<*, *> -> {
            this.startObject()
            obj.entries.forEach { (k,v) ->
                println(k.toString())
                this.field(k.toString())
                writeAny(v)
            }
            this.endObject()
        }
        is List<*> -> {
            this.startArray()
            obj.forEach {
                this.writeAny(it)
            }
            this.endArray()
        }
        is Sequence<*> -> {
            this.startArray()
            obj.forEach {
                this.writeAny(it)
            }
            this.endArray()
        }
        is Array<*> -> {
            this.startArray()
            obj.forEach {
                this.writeAny(it)
            }
            this.endArray()
        }
        else -> {
            throw IllegalArgumentException("Unsupported type: ${obj::class}")
        }
    }
}

fun XContentBuilder.obj(name: String, block: XContentBuilder.() -> Unit) {
    this.startObject(name)
    block.invoke(this)
    this.endObject()
}

fun XContentBuilder.arr(name: String, block: XContentBuilder.() -> Unit) {
    this.startArray(name)
    block.invoke(this)
    this.endArray()
}

fun xContent(obj: Any): XContentBuilder {
    val builder = JsonXContent.contentBuilder()
    builder.writeAny(obj)
    return builder

}
