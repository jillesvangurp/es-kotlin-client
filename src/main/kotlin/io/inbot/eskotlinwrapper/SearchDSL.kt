@file:Suppress("unused")

package io.inbot.eskotlinwrapper

@DslMarker
annotation class SearchDSLMarker

@SearchDSLMarker
open class ESQuery(val name: String, val queryDetails: MapBackedProperties = MapBackedProperties()) :
    MutableMap<String, Any> by queryDetails {

    fun toMap(): Map<String, MapBackedProperties> = mapOf(name to queryDetails)
}

fun matchAll() = ESQuery("match_all")

@SearchDSLMarker
class BoolQuery() : ESQuery(name = "bool") {
    fun should(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("should").addAll(q.map { it.toMap() })
    fun must(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must").addAll(q.map { it.toMap() })
    fun mustNot(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must_not").addAll(q.map { it.toMap() })
    fun filter(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("filter").addAll(q.map { it.toMap() })
}

// Begin MATCH_QUERY
@SearchDSLMarker
class MatchQueryConfig : MapBackedProperties() {
    var query by property<String>()
    var boost by property<Double>()
    // TODO support more of the different options in match
}

@SearchDSLMarker
class MatchQuery(
    field: String,
    query: String,
    val matchQueryConfig: MatchQueryConfig = MatchQueryConfig()
) : ESQuery(name = "match") {
    // The map is empty until we assign something
    init {
        this[field] = matchQueryConfig
        matchQueryConfig.query = query
    }

    var boost: Double by queryDetails.property()
}

fun match(
    field: String,
    value: String,
    block: (MatchQueryConfig.() -> Unit)? = null
): ESQuery {
    val q = MatchQuery(field, value)
    block?.invoke(q.matchQueryConfig)
    return q
}
// END MATCH_QUERY

class SearchDSL() : MapBackedProperties() {
    var from: Int by property()
    var resultSize: Int by property("size") // clashes with Map.size
    fun query(q: ESQuery) {
        this["query"] = q.toMap()
    }
}

fun bool(block: BoolQuery.() -> Unit): ESQuery {
    val q = BoolQuery()
    block.invoke(q)
    return q
}

fun customQuery(name: String, block: MapBackedProperties.() -> Unit): ESQuery {
    val q = ESQuery(name)
    block.invoke(q.queryDetails)
    return q
}

