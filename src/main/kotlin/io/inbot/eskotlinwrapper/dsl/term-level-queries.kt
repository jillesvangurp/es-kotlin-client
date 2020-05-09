@file:Suppress("unused")

package io.inbot.eskotlinwrapper.dsl

import io.inbot.eskotlinwrapper.MapBackedProperties

@SearchDSLMarker
class ExistsQuery(field: String) : ESQuery("exists") {
    init {
        this["field"] = field
    }

    val field = queryDetails["field"] as String
}

class FuzzyQueryConfig : MapBackedProperties() {
    var value by property<String>()
    var fuzziness by property<String>()
    var maxExpansions by property<Int>()
    var prefixLength by property<Int>()
    var transpositions by property<Boolean>()
    var rewrite by property<String>()
}

@SearchDSLMarker
class FuzzyQuery(
    field: String,
    value: String,
    fuzzyQueryConfig: FuzzyQueryConfig = FuzzyQueryConfig(),
    block: (FuzzyQueryConfig.() -> Unit)? = null
) :
    ESQuery("fuzzy") {
    init {
        this[field] = fuzzyQueryConfig
        fuzzyQueryConfig.value = value
        block?.invoke(fuzzyQueryConfig)
    }
}

@SearchDSLMarker
class IdsQuery(vararg values: String) : ESQuery("ids") {
    init {
        this["values"] = values
    }
}

class PrefixQueryConfig : MapBackedProperties() {
    var value by property<String>()
}

@SearchDSLMarker
class PrefixQuery(
    field: String,
    value: String,
    prefixQueryConfig: PrefixQueryConfig = PrefixQueryConfig(),
    block: (PrefixQueryConfig.() -> Unit)? = null
) : ESQuery("prefix") {
    init {
        this[field] = prefixQueryConfig
        prefixQueryConfig.value = value
        block?.invoke(prefixQueryConfig)
    }
}

enum class RangeRelation { INTERSECTS, CONTAINS, WITHIN }
class RangeQueryConfig : MapBackedProperties() {
    var boost by property<Double>()
    var gt by property<Any>()
    var gte by property<Any>()
    var lt by property<Any>()
    var lte by property<Any>()
    var format by property<String>()
    var relation by property<RangeRelation>()
    var timeZone by property<String>()
}

@SearchDSLMarker
class RangeQuery(
    field: String,
    rangeQueryConfig: RangeQueryConfig = RangeQueryConfig(),
    block: RangeQueryConfig.() -> Unit
) : ESQuery("range") {
    init {
        this[field] = rangeQueryConfig
        block.invoke(rangeQueryConfig)
    }
}

class RegExpQueryConfig : MapBackedProperties() {
    var value by property<String>()
    var flags by property<String>()
    var maxDeterminizedStates by property<Int>()
    var rewrite by property<String>()
}

@SearchDSLMarker
class RegExpQuery(
    field: String,
    value: String,
    regExpQueryConfig: RegExpQueryConfig = RegExpQueryConfig(),
    block: (RegExpQueryConfig.() -> Unit)? = null
) : ESQuery("regexp") {
    init {
        this[field] = regExpQueryConfig
        regExpQueryConfig.value = value
        block?.invoke(regExpQueryConfig)
    }
}

class TermQueryConfig : MapBackedProperties() {
    var value by property<String>()
    var boost by property<Double>()
}

@SearchDSLMarker
class TermQuery(
    field: String,
    value: String,
    termQueryConfig: TermQueryConfig = TermQueryConfig(),
    block: (TermQueryConfig.() -> Unit)? = null
) : ESQuery("term") {

    var boost by queryDetails.property<Double>()

    init {
        this[field] = termQueryConfig
        termQueryConfig.value = value
        block?.invoke(termQueryConfig)
    }
}

@SearchDSLMarker
class TermsQuery(
    field: String,
    vararg values: String,
    block: (TermsQuery.() -> Unit)? = null
) : ESQuery("terms") {
    var boost by queryDetails.property<Double>()
    var index by queryDetails.property<String>()
    var id by queryDetails.property<String>()
    var path by queryDetails.property<String>()
    var routing by queryDetails.property<String>()

    init {
        this[field] = values
        block?.invoke(this)
    }
}

class WildCardQueryConfig : MapBackedProperties() {
    var value by property<String>()
    var boost by property<Double>()
    var rewrite by property<String>()
}

@SearchDSLMarker
class WildCardQuery(
    field: String,
    value: String,
    wildCardQueryConfig: WildCardQueryConfig = WildCardQueryConfig(),
    block: (WildCardQueryConfig.() -> Unit)? = null
) : ESQuery("term") {

    var boost by queryDetails.property<Double>()

    init {
        this[field] = wildCardQueryConfig
        wildCardQueryConfig.value = value
        block?.invoke(wildCardQueryConfig)
    }
}
