package org.elasticsearch.action.search

import mu.KLogger
import mu.KotlinLogging
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.DeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.SearchModule
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.InputStream
import java.io.Reader
import java.util.Collections

private val logger: KLogger = KotlinLogging.logger { }

private val LOGGING_DEPRECATION_HANDLER: DeprecationHandler = object : DeprecationHandler {

    override fun usedDeprecatedField(usedName: String, replacedWith: String) {
        logger.warn { "You are using a deprecated field $usedName. You should use $replacedWith" }
    }

    override fun usedDeprecatedName(usedName: String, modernName: String) {
        logger.warn { "You are using a deprecated name $usedName. You should use $modernName" }
    }
}

private val searchModule = SearchModule(Settings.EMPTY, false, Collections.emptyList())

/**
 * Adds the missing piece to the SearchRequest API that allows you to paste raw json.
 * This makes sense in Kotlin because it has multiline strings and support for template variables.
 */
fun SearchRequest.source(json: String, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {
    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        json
    ).use {
        source(SearchSourceBuilder.fromXContent(it))
    }
}

/**
 * Adds the missing piece to the SearchRequest API that allows you to paste raw using a Reader. Useful if you store your queries in files.
 */
fun SearchRequest.source(reader: Reader, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {

    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        reader
    ).use {
        source(SearchSourceBuilder.fromXContent(it))
    }
}

/**
 * Supports taking the query straight from an InputStream. You probably should use the reader version.
 */
fun SearchRequest.source(
    inputStream: InputStream,
    deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER
) {
    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        inputStream
    ).use {
        source(SearchSourceBuilder.fromXContent(it))
    }
}
