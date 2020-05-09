@file:Suppress("unused")

package io.inbot.eskotlinwrapper.dsl

import io.inbot.eskotlinwrapper.MapBackedProperties

// Begin MATCH_QUERY
enum class MatchOperator { AND, OR }
@Suppress("EnumEntryName")
enum class ZeroTermsQuery { all, none }

@SearchDSLMarker
class MatchQueryConfig : MapBackedProperties() {
    var query by property<String>()
    var boost by property<Double>()
    var analyzer by property<String>()
    var autoGenerateSynonymsPhraseQuery by property<Boolean>()
    var fuzziness by property<String>()
    var maxExpansions by property<Int>()
    var prefixLength by property<Int>()
    var transpositions by property<Boolean>()
    var fuzzyRewrite by property<String>()
    var lenient by property<Boolean>()
    var operator by property<MatchOperator>()
    var minimumShouldMatch by property<String>()
    var zeroTermsQuery by property<ZeroTermsQuery>()
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
    var minimumShouldMatch by property<String>()
    var fuzziness by property<String>()
    var maxExpansions by property<Int>()
    var prefixLength by property<Int>()
    var transpositions by property<Boolean>()
    var fuzzyRewrite by property<String>()
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
