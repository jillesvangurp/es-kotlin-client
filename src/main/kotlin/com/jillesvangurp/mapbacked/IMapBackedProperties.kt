package com.jillesvangurp.mapbacked

import kotlin.properties.ReadWriteProperty

interface IMapBackedProperties : MutableMap<String, Any> {
    fun put(key: String, value: Any, namingConvention: PropertyNamingConvention)

    /**
     * Property delegate that stores the value in the MapBackedProperties. Use this to create type safe
     * properties.
     */
    fun <T : Any?> property(): ReadWriteProperty<Any, T>

    /**
     * Property delegate that stores the value in the MapBackedProperties; uses the customPropertyName instead of the
     * kotlin property name. Use this to create type safe properties in case the property name you need overlaps clashes
     * with a kotlin keyword or super class property or method. For example, "size" is also a method on
     * MapBackedProperties and thus cannot be used as a kotlin property name in a Kotlin class implementing Map.
     */
    fun <T : Any?> property(customPropertyName: String): ReadWriteProperty<MapBackedProperties, T>

    /**
     * Helper to manipulate list value objects.
     */
    fun getOrCreateMutableList(key: String): MutableList<Any>
}