package org.elasticsearch.action.search

import com.jillesvangurp.eskotlinwrapper.dsl.SearchDSL
import mu.KLogger
import mu.KotlinLogging
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.DeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentLocation
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.common.xcontent.stringify
import org.elasticsearch.index.mapper.MapperExtrasPlugin
import org.elasticsearch.search.SearchModule
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.InputStream
import java.io.Reader
import java.util.Collections
import java.util.function.Supplier

private val logger: KLogger = KotlinLogging.logger { }

val LOGGING_DEPRECATION_HANDLER: DeprecationHandler = object : DeprecationHandler {
    override fun usedDeprecatedName(
        parserName: String?,
        location: Supplier<XContentLocation>?,
        usedName: String?,
        modernName: String?
    ) {
        logger.warn { "You are using a deprecated name $usedName. You should use $modernName at ${location?.get()?.javaClass?.name}:${location?.get()?.lineNumber}" }
    }

    override fun usedDeprecatedField(
        parserName: String?,
        location: Supplier<XContentLocation>?,
        usedName: String?,
        replacedWith: String?
    ) {
        logger.warn { "You are using a deprecated field $usedName. You should use $replacedWith at ${location?.get()?.javaClass?.name}:${location?.get()?.lineNumber}" }
    }

    override fun usedDeprecatedField(parserName: String?, location: Supplier<XContentLocation>?, usedName: String?) {
        logger.warn { "You are using a deprecated field $usedName at ${location?.get()?.javaClass?.name}:${location?.get()?.lineNumber}" }
    }
}

private val searchModule = SearchModule(Settings.EMPTY, false, listOf(MapperExtrasPlugin()))

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
 * Adds the missing piece to the CountRequest API that allows you to paste raw json.
 * This makes sense in Kotlin because it has multiline strings and support for template variables.
 */
fun CountRequest.source(json: String, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {
    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        json
    ).use {
        query(SearchSourceBuilder.fromXContent(it).query())
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

fun SearchRequest.configure(pretty: Boolean = false, block: SearchDSL.() -> Unit): SearchDSL {
    val searchDSL = SearchDSL()
    block.invoke(searchDSL)
    searchDSL.toString()
    val query = searchDSL.stringify(pretty)
    source(query)
    return searchDSL
}

@Deprecated("Use SearchRequest.configure")
fun SearchRequest.dsl(pretty: Boolean = false, block: SearchDSL.() -> Unit): SearchDSL {
    val searchDSL = SearchDSL()
    block.invoke(searchDSL)
    val query = searchDSL.stringify(pretty)
    source(query)
    return searchDSL
}

/**
 * Adds the missing piece to the CountRequest API that allows you to paste raw using a Reader. Useful if you store your queries in files.
 */
fun CountRequest.source(reader: Reader, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {

    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        reader
    ).use {
        query(SearchSourceBuilder.fromXContent(it).query())
    }
}

fun CountRequest.configure(pretty: Boolean = false, block: SearchDSL.() -> Unit): SearchDSL {
    val searchDSL = SearchDSL()
    block.invoke(searchDSL)
    val query = searchDSL.stringify(pretty)
    source(query)
    return searchDSL
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

fun SearchRequest.source(block: SearchSourceBuilder.() -> Unit) {
    val builder = SearchSourceBuilder()
    block.invoke(builder)
    source(builder)
}

/**
 * Supports taking the query straight from an InputStream. You probably should use the reader version.
 */
fun CountRequest.source(
    inputStream: InputStream,
    deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER
) {
    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        inputStream
    ).use {
        query(SearchSourceBuilder.fromXContent(it).query())
    }
}

fun CountRequest.source(block: SearchSourceBuilder.() -> Unit) {
    val builder = SearchSourceBuilder()
    block.invoke(builder)
    query(builder.query())
}
