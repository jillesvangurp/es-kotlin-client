@file:Suppress("unused")

package com.jillesvangurp.es.querydsl

import com.jillesvangurp.mapbacked.IMapBackedProperties
import com.jillesvangurp.mapbacked.MapBackedProperties
import com.jillesvangurp.mapbacked.MapBackedProperties.Companion.create
import com.jillesvangurp.mapbacked.PropertyNamingConvention
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@DslMarker
annotation class SearchDSLMarker

@SearchDSLMarker
open class ESQuery(val name: String, val queryDetails: MapBackedProperties = MapBackedProperties(PropertyNamingConvention.ConvertToSnakeCase)) :
    IMapBackedProperties by queryDetails {

    fun toMap(): Map<String, Any> = create { this[name] = queryDetails }

    override fun toString(): String {
        return toMap().toString()
    }
}

@Suppress("UNCHECKED_CAST")
fun MapBackedProperties.esQueryProperty(): ReadWriteProperty<Any, ESQuery> {
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

fun customQuery(name: String, block: MapBackedProperties.() -> Unit): ESQuery {
    val q = ESQuery(name)
    block.invoke(q.queryDetails)
    return q
}

@Suppress("UNCHECKED_CAST")
class SearchDSL : MapBackedProperties(PropertyNamingConvention.ConvertToSnakeCase) {
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
@Suppress("UNUSED_PARAMETER")
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
    ) = sortFields.add(create {
        this.put(field, create {
            this["order"] = order.name
            mode?.let {
                this["mode"] = mode.name.lowercase(Locale.getDefault())
            }
            block?.invoke(this)
        }, PropertyNamingConvention.AsIs)
    })
}

fun SearchDSL.sort(block: SortBuilder.() -> Unit) {
    val builder =  SortBuilder()
    block.invoke(builder)
    this["sort"] = builder.sortFields
}

fun SearchDSL.matchAll() = ESQuery("match_all")

@Suppress("EnumEntryName")
enum class ExpandWildCards { all, open, closed, hidden, none }

@Suppress("EnumEntryName")
enum class SearchType { query_then_fetch, dfs_query_then_fetch }

@Suppress("SpellCheckingInspection")
class MultiSearchHeader : MapBackedProperties(PropertyNamingConvention.ConvertToSnakeCase) {
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


