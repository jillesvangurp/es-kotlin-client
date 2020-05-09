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

class MatchPhrasePrefixQueryConfig : MapBackedProperties() {
    var query by property<String>()
    var boost by property<Double>()
    var analyzer by property<String>()
    var maxExpansions by property<Int>()
    var slop by property<Int>()
    var zeroTermsQuery by property<ZeroTermsQuery>()
}

class MatchPhrasePrefixQuery(
    field: String,
    query: String,
    internal val matchPhrasePrefixQueryConfig: MatchPhrasePrefixQueryConfig = MatchPhrasePrefixQueryConfig()
) : ESQuery(name = "match_phrase_prefix") {
    init {
        this[field] = matchPhrasePrefixQueryConfig
        matchPhrasePrefixQueryConfig.query = query
    }
}

fun matchPhrasePrefix(
    field: String,
    value: String,
    block: (MatchPhrasePrefixQueryConfig.() -> Unit)? = null
): MatchPhrasePrefixQuery {
    val q = MatchPhrasePrefixQuery(field, value)
    block?.invoke(q.matchPhrasePrefixQueryConfig)
    return q
}

@Suppress("EnumEntryName")
enum class MultiMatchType {
    best_fields,most_fields,cross_fields,phrase,phrase_prefix,bool_prefix
}
class MultiMatchQuery(query: String, vararg fields: String) : ESQuery("multi_match") {
    init {
        this["query"] = query
        this["fields"] = fields
    }
    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>


    var type by queryDetails.property<MultiMatchType>()
    // note not all of these are usable with all types; check documentation
    var tieBreaker by queryDetails.property<Double>()
    var analyzer by queryDetails.property<String>()
    var autoGenerateSynonymsPhraseQuery by queryDetails.property<Boolean>()
    var fuzziness by queryDetails.property<String>()
    var maxExpansions by queryDetails.property<Int>()
    var prefixLength by queryDetails.property<Int>()
    var transpositions by queryDetails.property<Boolean>()
    var fuzzyRewrite by queryDetails.property<String>()
    var lenient by queryDetails.property<Boolean>()
    var operator by queryDetails.property<MatchOperator>()
    var minimumShouldMatch by queryDetails.property<String>()
    var zeroTermsQuery by queryDetails.property<ZeroTermsQuery>()
    var slop by queryDetails.property<Int>()
}

fun multiMatch(
    query: String,
    vararg fields: String,
    block: (MultiMatchQuery.() -> Unit)? = null
): MultiMatchQuery {
    val q = MultiMatchQuery(query, *fields)
    block?.invoke(q)
    return q
}

class QueryStringQuery(query: String, vararg fields: String) : ESQuery("query_string") {
    init {
        this["query"] = query
        this["fields"] = fields
    }

    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>

    var defaultField by queryDetails.property<String>()
    var allowLeadingWildcard by queryDetails.property<Boolean>()
    var analyzeWildcard by queryDetails.property<Boolean>()
    var analyzer by queryDetails.property<String>()
    var autoGenerateSynonymsPhraseQuery by queryDetails.property<Boolean>()
    var fuzziness by queryDetails.property<String>()
    var maxExpansions by queryDetails.property<Int>()
    var prefixLength by queryDetails.property<Int>()
    var transpositions by queryDetails.property<Boolean>()
    var fuzzyRewrite by queryDetails.property<String>()
    var lenient by queryDetails.property<Boolean>()
    var defaultOperator by queryDetails.property<MatchOperator>()
    var minimumShouldMatch by queryDetails.property<String>()
    var zeroTermsQuery by queryDetails.property<ZeroTermsQuery>()
    var maxDeterminizedStates by queryDetails.property<Int>()
    var quoteAnalyzer by queryDetails.property<String>()
    var phraseSlop by queryDetails.property<Int>()
    var quoteFieldSuffix by queryDetails.property<String>()
    var rewrite by queryDetails.property<String>()
    var timeZone by queryDetails.property<String>()
}

fun queryString(
    query: String,
    vararg fields: String,
    block: (QueryStringQuery.() -> Unit)? = null
): QueryStringQuery {
    val q = QueryStringQuery(query, *fields)
    block?.invoke(q)
    return q
}

class SimpleQueryStringQuery(query: String, vararg fields: String) : ESQuery("simple_query_string") {
    init {
        this["query"] = query
        this["fields"] = fields
    }

    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>

    var defaultField by queryDetails.property<String>()
    var allFields by queryDetails.property<Boolean>()

    var flags by queryDetails.property<String>()
    var analyzeWildcard by queryDetails.property<Boolean>()
    var analyzer by queryDetails.property<String>()
    var autoGenerateSynonymsPhraseQuery by queryDetails.property<Boolean>()
    var fuzziness by queryDetails.property<String>()
    var fuzzyTranspositions by queryDetails.property<Boolean>()
    var fuzzyMaxExpansions by queryDetails.property<Int>()
    var fuzzyPrefixLength by queryDetails.property<Int>()
    var lenient by queryDetails.property<Boolean>()
    var defaultOperator by queryDetails.property<MatchOperator>()
    var minimumShouldMatch by queryDetails.property<String>()
    var quoteFieldSuffix by queryDetails.property<String>()
}
