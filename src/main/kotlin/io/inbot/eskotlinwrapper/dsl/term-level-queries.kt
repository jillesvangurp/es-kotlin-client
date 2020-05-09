@file:Suppress("unused")

package io.inbot.eskotlinwrapper.dsl

import io.inbot.eskotlinwrapper.MapBackedProperties

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

class FuzzyQuery(
    field: String,
    value: String,
    internal val fuzzyQueryConfig: FuzzyQueryConfig = FuzzyQueryConfig(),
    block: (FuzzyQueryConfig.() -> Unit)? = null
) :
    ESQuery("fuzzy") {
    init {
        this[field] = fuzzyQueryConfig
        fuzzyQueryConfig.value = value
        block?.invoke(fuzzyQueryConfig)
    }
}

class IdsQuery(vararg values: String) : ESQuery("ids") {
    init {
        this["values"] = values
    }
}

class PrefixQueryConfig : MapBackedProperties() {
    var value by property<String>()
}

class PrefixQuery(
    field: String,
    value: String,
    internal val prefixQueryConfig: PrefixQueryConfig = PrefixQueryConfig(),
    block: (PrefixQueryConfig.() -> Unit)? = null
) : ESQuery("prefix") {
    init {
        this[field] = prefixQueryConfig
        prefixQueryConfig.value = value
        block?.invoke(prefixQueryConfig)
    }
}
