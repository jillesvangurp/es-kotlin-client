package com.jillesvangurp.eskotlinwrapper

// for our tests lets make this mutable
data class TestModel(
    var message: String,
    var tag: String? = null,
    var number: Double? = null,
    var people: List<PeopleModel>? = null,
)

data class PeopleModel(var gender: String, var percentage: Int)
