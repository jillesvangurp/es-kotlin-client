@file:Suppress("unused")

package com.jillesvangurp.eskotlinwrapper.dsl

import com.jillesvangurp.eskotlinwrapper.IMapBackedProperties
import com.jillesvangurp.eskotlinwrapper.MapBackedProperties
import com.jillesvangurp.eskotlinwrapper.mapProps
import org.elasticsearch.common.xcontent.stringify

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


}

fun SearchDSL.matchAll() = ESQuery("match_all")

fun main() {
    val d = SearchDSL()
    d.apply {
        query = matchAll()
    }

    println(d.stringify(true))
}
