package io.inbot.eskotlinwrapper.manual

import org.apache.commons.lang3.StringUtils


class KotlinForExample private constructor(private val fileName: String,  title: String) : AutoCloseable {

    private val buf = StringBuilder()

    init {
        buf.appendln("# $title")
        buf.appendln()
    }

    fun <T> blockWithOutput(name:String, block: () -> T) {
        val caller = Thread.currentThread()
            .stackTrace.first { it.className != this.javaClass.name && it.className != "java.lang.Thread" }
            .className.replace("\\$.*?$".toRegex(), "") // we don't want the inner class

        buf.appendln("```kotlin")
        buf.appendln("""println("$caller")""")
        buf.appendln("```")
        buf.appendln()
        val response = block.invoke()

        val stringified = response.toString()
        if(stringified != "kotlin.Unit")  {
            buf.appendln("Produces:\n\n```\n$stringified\n```")
        }
    }

    override fun close() {
        println(fileName)
        println("${buf.toString()}")
    }


    companion object {
        fun example(
            title: String,
            fileName: String = StringUtils.lowerCase(
                title.replace(' ', '-').replace(
                    "[^A-Za-z0-9\\-]".toRegex(),
                    ""
                ) + ".md"
            ),
            block: KotlinForExample.() -> Unit
        ) {
            val example = KotlinForExample(fileName, title)
            example.use(block)
        }
    }
}