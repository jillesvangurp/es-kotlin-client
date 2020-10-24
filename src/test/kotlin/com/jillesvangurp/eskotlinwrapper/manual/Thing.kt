package com.jillesvangurp.eskotlinwrapper.manual

// BEGIN thing-class
// given some model class with two fields
data class Thing(
    val name: String,
    val amount: Long = 42
)
// END thing-class
