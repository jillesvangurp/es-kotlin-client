package io.inbot.eskotlinwrapper

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogger
import mu.KotlinLogging
import org.apache.http.Header
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.xcontent.DeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.SearchHits
import org.elasticsearch.search.SearchModule
import org.elasticsearch.search.builder.SearchSourceBuilder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.Reader
import java.nio.charset.StandardCharsets
import java.util.Collections

private val logger: KLogger = KotlinLogging.logger { }

fun <T : Any> RestHighLevelClient.crudDao(
    index: String,
    modelReaderAndWriter: ModelReaderAndWriter<T>,
    refreshAllowed: Boolean = false
): IndexDAO<T> {
    return IndexDAO(index, this, modelReaderAndWriter, refreshAllowed = refreshAllowed)
}

inline fun <reified T : Any> RestHighLevelClient.crudDao(
    index: String,
    objectMapper: ObjectMapper = ObjectMapper().findAndRegisterModules(),
    refreshAllowed: Boolean = false
): IndexDAO<T> {
    return IndexDAO(
        index,
        this,
        JacksonModelReaderAndWriter(T::class, objectMapper),
        refreshAllowed = refreshAllowed
    )
}

fun RestHighLevelClient.doSearch(headers: List<Header> = listOf(), block: SearchRequest.() -> Unit): SearchResponse {
    val searchRequest = SearchRequest()
    block.invoke(searchRequest)
    return this.search(searchRequest, *headers.toTypedArray())
}

fun <T : Any> SearchHits.mapHits(fn: (SearchHit) -> T): List<T> {
    return this.hits.map(fn)
}

fun <T : Any> SearchHits.mapHits(modelReaderAndWriter: ModelReaderAndWriter<T>): Sequence<T> {
    return this.hits.asSequence()
        .map({ it -> modelReaderAndWriter.deserialize(it) })
}

private val LOGGING_DEPRECATION_HANDLER: DeprecationHandler = object : DeprecationHandler {
    override fun usedDeprecatedField(usedName: String, replacedWith: String) {
        logger.warn { "You are using a deprecated field $usedName. You should use $replacedWith" }
    }

    override fun usedDeprecatedName(usedName: String, modernName: String) {
        logger.warn { "You are using a deprecated name $usedName. You should use $modernName" }
    }
}

private val searchModule = SearchModule(Settings.EMPTY, false, Collections.emptyList())

fun SearchRequest.source(json: String, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {
    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        json
    ).use {
        source(SearchSourceBuilder.fromXContent(it))
    }
}

fun SearchRequest.source(reader: Reader, deprecationHandler: DeprecationHandler = LOGGING_DEPRECATION_HANDLER) {

    XContentFactory.xContent(XContentType.JSON).createParser(
        NamedXContentRegistry(searchModule.namedXContents),
        deprecationHandler,
        reader
    ).use {
        source(SearchSourceBuilder.fromXContent(it))
    }
}

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
