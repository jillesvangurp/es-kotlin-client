package com.jillesvangurp.es.querydsl

import com.jillesvangurp.mapbacked.stringify

class MultiSearchDSL {
    private val json = mutableListOf<String>()
    fun add(header: MultiSearchHeader, query: SearchDSL) {
        json.add(header.stringify(false))
        json.add(query.stringify(false))
    }

    fun add(header: MultiSearchHeader, queryBlock: SearchDSL.() -> Unit) {
        json.add(header.stringify(false))
        val dsl = SearchDSL()
        queryBlock.invoke(dsl)
        json.add(dsl.stringify(false))
    }

    fun header(headerBlock: MultiSearchHeader.()-> Unit) : MultiSearchHeader {
        val header = MultiSearchHeader()
        headerBlock.invoke(header)
        return header
    }

    infix fun MultiSearchHeader.withQuery(queryBlock: SearchDSL.()-> Unit) {
        val dsl = SearchDSL()
        queryBlock.invoke(dsl)
        add(this, dsl)
    }

    fun withQuery(queryBlock: SearchDSL.()-> Unit) {
        val dsl = SearchDSL()
        queryBlock.invoke(dsl)
        add(MultiSearchHeader(), dsl)
    }

    fun requestBody(): String {
        return json.joinToString("\n") + "\n"
    }

    override fun toString(): String {
        return requestBody()
    }
}