package com.jillesvangurp.mapbackedproperties

interface DslSerializer {
    fun serialize(properties: MapBackedProperties, pretty: Boolean=false): String
}
