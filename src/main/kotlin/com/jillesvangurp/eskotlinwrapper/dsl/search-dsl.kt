@file:Suppress("unused")

package com.jillesvangurp.eskotlinwrapper.dsl

import com.jillesvangurp.eskotlinwrapper.IMapBackedProperties
import com.jillesvangurp.eskotlinwrapper.MapBackedProperties
import com.jillesvangurp.eskotlinwrapper.mapProps
import org.elasticsearch.common.xcontent.stringify
import java.util.*
import kotlin.reflect.KProperty

@DslMarker
annotation class SearchDSLMarker

@SearchDSLMarker
open class ESQuery(val name: String, val queryDetails: MapBackedProperties = MapBackedProperties()) :
    IMapBackedProperties by queryDetails {

    fun toMap(): Map<String, Any> = mapProps { this[name] = queryDetails }

    override fun toString(): String {
        return toMap().toString()
    }
}

fun customQuery(name: String, block: MapBackedProperties.() -> Unit): ESQuery {
    val q = ESQuery(name)
    block.invoke(q.queryDetails)
    return q
}

@Suppress("UNCHECKED_CAST")
class SearchDSL : MapBackedProperties() {
    var from: Int by property()
    var trackTotalHits: Boolean by property()

    /** Same as the size property on Elasticsearch. But as kotlin's map already has a size property, we can't use that name. */
    var resultSize: Int by property("size") // clashes with Map.size

    // Elasticsearch has this object in a object kind of thing that we need to compensate for.
    var query: ESQuery
        get() {
            val map = this["query"] as Map<String, MapBackedProperties>
            val (name, queryDetails) = map.entries.first()
            return ESQuery(name, queryDetails)
        }
        set(value) {
            this["query"] = value.toMap()
        }

    var postFilter: ESQuery
        get() {
            val map = this["post_filter"] as Map<String, MapBackedProperties>
            val (name, queryDetails) = map.entries.first()
            return ESQuery(name, queryDetails)
        }
        set(value) {
            this["post_filter"] = value.toMap()
        }
}

enum class SortOrder { ASC, DESC }
enum class SortMode { MIN, MAX, SUM, AVG, MEDIAN }
class SortField(field: String, order: SortOrder? = null, mode: SortMode? = null)

class SortBuilder {
    internal val sortFields = mutableListOf<Any>()

    operator fun String.unaryPlus() = sortFields.add(this)
    operator fun KProperty<*>.unaryPlus() = sortFields.add(this)

    fun add(field: KProperty<*>, order: SortOrder = SortOrder.DESC, mode: SortMode? = null, block: (MapBackedProperties.() -> Unit)? = null) =
        add(field.name, order, mode, block)

    fun add(
        field: String,
        order: SortOrder,
        mode: SortMode?,
        block: (MapBackedProperties.() -> Unit)?
    ) = sortFields.add(mapProps {
        this.putNoSnakeCase(field, mapProps {
            this["order"] = order.name
            mode?.let {
                this["mode"] = mode.name.lowercase(Locale.getDefault())
            }
            block?.invoke(this)
        })
    })
}

fun SearchDSL.sort(block: SortBuilder.() -> Unit) {
    val builder =  SortBuilder()
    block.invoke(builder)
    this["sort"] = builder.sortFields
}

fun SearchDSL.matchAll() = ESQuery("match_all")

fun main() {
    val d = SearchDSL()
    d.apply {
        query = matchAll()
    }

    println(d.stringify(true))
}

@Suppress("EnumEntryName")
enum class ExpandWildCards { all, open, closed, hidden, none }

@Suppress("EnumEntryName")
enum class SearchType { query_then_fetch, dfs_query_then_fetch }

@Suppress("SpellCheckingInspection")
class MultiSearchHeader : MapBackedProperties() {
    var allowNoIndices by property<Boolean>()
    var ccsMinimizeRoundtrips by property<Boolean>()
    var expandWildcards by property<ExpandWildCards>()
    var ignoreThrottled by property<Boolean>()
    var ignoreUnavailable by property<Boolean>()
    var maxConcurrentSearches by property<Int>()
    var maxConcurrentShardRequests by property<Int>()
    var preFilterShardSize by property<Int>()
    var restTotalHitsAsInt by property<Int>()
    var routing by property<String>()
    var searchType by property<SearchType>()
    var typedKeys by property<Boolean>()
}

class MultiSearchDSL {
    private val json = mutableListOf<String>()
    fun add(header: MultiSearchHeader, query: SearchDSL) {
        json.add(header.stringify(false))
        json.add(query.stringify(false))
    }

    fun add(header: MultiSearchHeader, query: SearchDSL.() -> Unit) {
        json.add(header.stringify(false))

    }

    fun header(headerBlock: MultiSearchHeader.()-> Unit) : MultiSearchHeader {
        val header = MultiSearchHeader()
        headerBlock.invoke(header)
        return header
    }

    infix fun MultiSearchHeader.withQuery(queryBlock: SearchDSL.()-> Unit) {
        val dsl = SearchDSL()
        queryBlock.invoke(dsl)
        add(this, dsl)
    }

    fun withQuery(queryBlock: SearchDSL.()-> Unit) {
        val dsl = SearchDSL()
        queryBlock.invoke(dsl)
        add(MultiSearchHeader(), dsl)
    }

    fun requestBody(): String {
        return json.joinToString("\n") + "\n"
    }

    override fun toString(): String {
        return requestBody()
    }
}
