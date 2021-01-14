@file:Suppress("unused", "UNCHECKED_CAST")

package com.jillesvangurp.eskotlinwrapper.dsl

import com.jillesvangurp.eskotlinwrapper.MapBackedProperties

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
    matchQueryConfig: MatchQueryConfig = MatchQueryConfig(),
    block: (MatchQueryConfig.() -> Unit)? = null
) : ESQuery(name = "match") {
    // The map is empty until we assign something
    init {
        putNoSnakeCase(field, matchQueryConfig)
        matchQueryConfig.query = query
        block?.invoke(matchQueryConfig)
    }
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
    matchBoolPrefixQueryConfig: MatchBoolPrefixQueryConfig = MatchBoolPrefixQueryConfig(),
    block: (MatchBoolPrefixQueryConfig.() -> Unit)? = null
) : ESQuery(name = "match_bool_prefix") {
    init {
        putNoSnakeCase(field, matchBoolPrefixQueryConfig)
        matchBoolPrefixQueryConfig.query = query
        block?.invoke(matchBoolPrefixQueryConfig)
    }
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
    matchPhrasePrefixQueryConfig: MatchPhrasePrefixQueryConfig = MatchPhrasePrefixQueryConfig(),
    block: (MatchPhrasePrefixQueryConfig.() -> Unit)? = null
) : ESQuery(name = "match_phrase_prefix") {
    init {
        putNoSnakeCase(field, matchPhrasePrefixQueryConfig)
        matchPhrasePrefixQueryConfig.query = query
        block?.invoke(matchPhrasePrefixQueryConfig)
    }
}

@Suppress("EnumEntryName")
enum class MultiMatchType {
    best_fields, most_fields, cross_fields, phrase, phrase_prefix, bool_prefix
}

class MultiMatchQuery(
    query: String,
    vararg fields: String,
    block: (MultiMatchQuery.() -> Unit)? = null
) : ESQuery("multi_match") {
    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>

    var type by queryDetails.property<MultiMatchType>()
    var boost by queryDetails.property<Double>()

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

    init {
        this["query"] = query
        this["fields"] = fields
        block?.invoke(this)
    }
}

class QueryStringQuery(
    query: String,
    vararg fields: String,
    block: (QueryStringQuery.() -> Unit)? = null
) : ESQuery("query_string") {

    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>
    var boost by queryDetails.property<Double>()

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

    init {
        this["query"] = query
        this["fields"] = fields
        block?.invoke(this)
    }
}

class SimpleQueryStringQuery(
    query: String,
    vararg fields: String,
    block: (SimpleQueryStringQuery.() -> Unit)? = null
) :
    ESQuery("simple_query_string") {
    val query: String get() = this["query"] as String
    val fields: Array<String> get() = this["fields"] as Array<String>
    var boost by queryDetails.property<Double>()

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

    init {
        this["query"] = query
        this["fields"] = fields
        block?.invoke(this)
    }
}
