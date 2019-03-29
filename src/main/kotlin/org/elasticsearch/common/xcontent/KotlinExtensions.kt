package org.elasticsearch.common.xcontent

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

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
