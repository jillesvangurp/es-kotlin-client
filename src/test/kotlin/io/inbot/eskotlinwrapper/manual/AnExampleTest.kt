package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test

class AnExampleTest {
    @Test
    fun `hello world`() {
        KotlinForExample.example("Hello World!") {
            blockWithOutput("An Example") {
                println("Hello World!")

            }
            blockWithOutput("Another Example") {
                println("OhHAI")
                "Hi"
            }
        }
    }
}