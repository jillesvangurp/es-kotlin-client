@file:Suppress("unused")

package io.inbot.eskotlinwrapper

@DslMarker
annotation class SearchDSLMarker

@SearchDSLMarker
open class ESQuery(val name: String, val queryDetails: MapBackedProperties = MapBackedProperties()) :
    MutableMap<String, Any> by queryDetails {

    fun toMap(): Map<String, MapBackedProperties> = mapOf(name to queryDetails)

    // helpers to easily construct ESQuery instances of different types
    // all ESQuery instances are mutable maps so you can tweak them
    // for unsupported query types, you can use the custom method
    companion object {
        fun matchAll() = ESQuery("match_all")

        fun bool(block: BoolQuery.() -> Unit): ESQuery {
            val bq = BoolQuery()
            block.invoke(bq)
            return bq
        }

        fun term(field: String, value: String, block: (MapBackedProperties.() -> Unit)? = null): ESQuery {
            val tq = TermQuery(field, value)
            block?.invoke(tq.termProps)
            return tq
        }

        fun match(field: String, value: String, block: TermQuery.() -> Unit): ESQuery {
            val tq = TermQuery(field, value)
            block.invoke(tq)
            return tq
        }

        fun custom(name: String, block: MapBackedProperties.() -> Unit): ESQuery {
            val q = ESQuery(name)
            block.invoke(q.queryDetails)
            return q
        }
    }

}

@SearchDSLMarker
class BoolQuery() : ESQuery(name = "bool") {
    fun should(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("should").addAll(q.map { it.toMap() })
    fun must(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must").addAll(q.map { it.toMap() })
    fun mustNot(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must_not").addAll(q.map { it.toMap() })
    fun filter(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("filter").addAll(q.map { it.toMap() })
}

@SearchDSLMarker
class TermQuery(field: String, value: String) : ESQuery(name = "term") {
    internal val termProps = MapBackedProperties()
    var value by termProps.property<String>()

    init {
        this[field] = field
        this.value = value
    }
}

@SearchDSLMarker
class MatchQuery(field: String, value: String) : ESQuery(name = "match") {
    init {
        this["field"] = field
        this["value"] = value
    }
}

class SearchDSL() : MapBackedProperties() {
    var from: Int by property()
    var resultSize: Int by property("size") // clashes with Map.size

    fun query(q: ESQuery) {
        this["query"] = q.toMap()
    }
}