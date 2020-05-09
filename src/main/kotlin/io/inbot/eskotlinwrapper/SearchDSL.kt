@file:Suppress("unused", "PropertyName", "EnumEntryName")

package io.inbot.eskotlinwrapper

@DslMarker
annotation class SearchDSLMarker

@SearchDSLMarker
open class ESQuery(val name: String, val queryDetails: MapBackedProperties = MapBackedProperties()) :
    MutableMap<String, Any> by queryDetails {

    fun toMap(): Map<String, MapBackedProperties> = mapOf(name to queryDetails)
}

fun customQuery(name: String, block: MapBackedProperties.() -> Unit): ESQuery {
    val q = ESQuery(name)
    block.invoke(q.queryDetails)
    return q
}

class SearchDSL() : MapBackedProperties() {
    var from: Int by property()
    /** Same as the size property on Elasticsearch. But as kotlin's map already has a size property, we can't use that name. */
    var resultSize: Int by property("size") // clashes with Map.size
    var query by property<ESQuery>()
}

@SearchDSLMarker
class BoolQuery() : ESQuery(name = "bool") {
    fun should(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("should").addAll(q.map { it.toMap() })
    fun must(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must").addAll(q.map { it.toMap() })
    fun mustNot(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("must_not").addAll(q.map { it.toMap() })
    fun filter(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("filter").addAll(q.map { it.toMap() })
}

fun bool(block: BoolQuery.() -> Unit): BoolQuery {
    val q = BoolQuery()
    block.invoke(q)
    return q
}

class BoostingQuery() : ESQuery(name = "boosting") {
    var positive: ESQuery by queryDetails.property()
    var negative: ESQuery by queryDetails.property()
    var negative_boost: Double by queryDetails.property()
}

fun boosting(block: BoostingQuery.() -> Unit): BoostingQuery {
    val q = BoostingQuery()
    block.invoke(q)
    return q
}

class ConstantScoreQuery() : ESQuery(name = "constant_score") {
    var filter: ESQuery by queryDetails.property()
    var boost: Double by queryDetails.property()
}

fun constantScore(block: ConstantScoreQuery.() -> Unit): ConstantScoreQuery {
    val q = ConstantScoreQuery()
    block.invoke(q)
    return q
}

class DisMaxQuery() : ESQuery(name = "dis_max") {
    fun queries(vararg q: ESQuery) = queryDetails.getOrCreateMutableList("queries").addAll(q.map { it.toMap() })
    var tie_breaker: Double by queryDetails.property()
}

fun disMax(block: DisMaxQuery.() -> Unit): DisMaxQuery {
    val q = DisMaxQuery()
    block.invoke(q)
    return q
}

fun matchAll() = ESQuery("match_all")

// Begin MATCH_QUERY
enum class MatchOperator { AND, OR }
enum class ZeroTermsQuery { all, none }

@SearchDSLMarker
class MatchQueryConfig : MapBackedProperties() {
    var query by property<String>()
    var boost by property<Double>()
    var analyzer by property<String>()
    var auto_generate_synonyms_phrase_query by property<Boolean>()
    var fuzziness by property<String>()
    var max_expansions by property<Int>()
    var prefix_length by property<Int>()
    var transpositions by property<Boolean>()
    var fuzzy_rewrite by property<String>()
    var lenient by property<Boolean>()
    var operator by property<MatchOperator>()
    var minimum_should_match by property<String>()
    var zero_terms_query by property<ZeroTermsQuery>()
}

@SearchDSLMarker
class MatchQuery(
    field: String,
    query: String,
    internal val matchQueryConfig: MatchQueryConfig = MatchQueryConfig()
) : ESQuery(name = "match") {
    // The map is empty until we assign something
    init {
        this[field] = matchQueryConfig
        matchQueryConfig.query = query
    }
}

fun match(
    field: String,
    value: String,
    block: (MatchQueryConfig.() -> Unit)? = null
): MatchQuery {
    val q = MatchQuery(field, value)
    block?.invoke(q.matchQueryConfig)
    return q
}
// END MATCH_QUERY

class MatchBoolPrefixQueryConfig : MapBackedProperties() {
    var query by property<String>()
    var boost by property<Double>()
    var analyzer by property<String>()
    var operator by property<MatchOperator>()
    var minimum_should_match by property<String>()
    var fuzziness by property<String>()
    var max_expansions by property<Int>()
    var prefix_length by property<Int>()
    var transpositions by property<Boolean>()
    var fuzzy_rewrite by property<String>()
}

class MatchBoolPrefixQuery(
    field: String,
    query: String,
    internal val matchBoolPrefixQueryConfig: MatchBoolPrefixQueryConfig = MatchBoolPrefixQueryConfig()
) : ESQuery(name = "match_bool_prefix") {
    init {
        this[field] = matchBoolPrefixQueryConfig
        matchBoolPrefixQueryConfig.query = query
    }
}

fun matchBoolPrefix(
    field: String,
    value: String,
    block: (MatchBoolPrefixQueryConfig.() -> Unit)? = null
): MatchBoolPrefixQuery {
    val q = MatchBoolPrefixQuery(field, value)
    block?.invoke(q.matchBoolPrefixQueryConfig)
    return q
}
