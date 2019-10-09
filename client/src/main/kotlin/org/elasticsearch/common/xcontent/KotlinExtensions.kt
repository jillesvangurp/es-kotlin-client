package org.elasticsearch.common.xcontent

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
