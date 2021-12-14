package com.jillesvangurp.eskotlinwrapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class MapBackedPropertiesTest {

    @Test
    fun `should convert property names`() {
        assertThat("fooBarFooBar".snakeCaseToUnderscore()).isEqualTo("foo_bar_foo_bar")
        assertThat("foo_BarFooBar".snakeCaseToUnderscore()).isEqualTo( "foo_bar_foo_bar")
        assertThat("foo1Bar1Foo1Bar".snakeCaseToUnderscore()).isEqualTo( "foo1_bar1_foo1_bar")
    }
}