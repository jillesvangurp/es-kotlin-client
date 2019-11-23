package io.inbot.eskotlinwrapper.manual

import mu.KLogger
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger { }

fun mdLink(title: String, target: String) = "[$title]($target)"

data class Page(
    val title: String,
    val fileName: String,
    val outputDir: String = "manual",
    val previous: String? = null,
    val next: String? = null,
    val parent: String? = null
) {
    val link = mdLink(title, fileName)
}

fun mdLink(page: Page) = mdLink(page.title, page.fileName)

@Suppress("MemberVisibilityCanBePrivate")
class KotlinForExample private constructor(
    private val sourcePaths: List<String> = listOf("src/main/kotlin", "src/test/kotlin"),
    private val repoUrl: String="https://github.com/jillesvangurp/es-kotlin-wrapper-client"
) : AutoCloseable {
    private val buf = StringBuilder()
    private val patternForBlock = "block.*?\\{+".toRegex(RegexOption.MULTILINE)

    operator fun String.unaryPlus() {
        buf.appendln(this.trimIndent().trimMargin())
        buf.appendln()
    }

    fun mdCodeBlock(code: String, type: String = "kotlin") {
        buf.appendln("```$type\n$code\n```\n")
    }

    fun mdLink(clazz: KClass<*>): String {
        val fileName = clazz.qualifiedName!!.replace("\\$.*?$".toRegex(), "").replace('.', File.separatorChar) + ".kt"

        return mdLink("`${clazz.simpleName!!}`","$repoUrl/tree/master/${sourcePaths.map { File(it,fileName) }.first { it.exists() }.path}")
    }

    fun mdLinkToSelf(title: String="Link to this source file"): String {
        val fn = this.sourceFile() ?: throw IllegalStateException("source file not found")
        return mdLink(title,"$repoUrl/tree/master/${fn.path}")
    }

    fun <T> block(runBlock: Boolean = false, block: () -> T) {
        val callerSourceBlock = getCallerSourceBlock()
        if (callerSourceBlock == null) {
            // we are assuming a few things about the caller source:
            // - MUST be a class with its own source file
            // - The source file must be in the sourcePaths
            logger.warn { "Could not find code block from stack trace and sourcePath" }
        } else {
            mdCodeBlock(callerSourceBlock)
        }

        if (runBlock) {
            val response = block.invoke()

            val returnValue = response.toString()
            if (returnValue != "kotlin.Unit") {
                buf.appendln("Produces:\n")
                mdCodeBlock(returnValue, type = "")
            }
        }
    }

    fun blockWithOutput(block: PrintWriter.() -> Unit) {
        val callerSourceBlock = getCallerSourceBlock()

        val outputBuffer = ByteArrayOutputStream()
        val writer = PrintWriter(outputBuffer.writer())
        writer.use {
            block.invoke(writer)
            if (callerSourceBlock == null) {
                logger.warn { "Could not find code block from stack trace and sourcePath" }
            } else {
                mdCodeBlock(callerSourceBlock)
            }
            writer.flush()
        }
        val output = outputBuffer.toString()
        if (output.isNotEmpty()) {
            buf.appendln("Output:\n")
            mdCodeBlock(output, type = "")
        }
    }

    private fun getCallerSourceBlock(): String? {
        val ste = getCallerStackTraceElement()
        val line = ste.lineNumber
        val sourceFile = ste.className.replace("\\$.*?$".toRegex(), "").replace('.', File.separatorChar) + ".kt"
        val lines = sourcePaths.map { File(it, sourceFile) }.firstOrNull { it.exists() }?.readLines()
        if (lines != null && line > 0) {
            // off by one error. Line numbers start at 1; list numbers start at 0
            val source = lines.subList(line - 1, lines.size).joinToString("\n")
            val allBlocks = patternForBlock.findAll(source)
            val match = allBlocks.first()
            val start = match.range.last
            var openCount = 1
            var index = start + 1
            while (openCount > 0 && index < source.length) {
                when (source[index++]) {
                    '{' -> openCount++
                    '}' -> openCount--
                }
            }
            if (index > start + 1 && index < source.length) {
                return source.substring(start + 1, index - 1).trimIndent()
            }
        }
        return null
    }

    internal fun sourceFile(): File? {
        val fileName = getCallerStackTraceElement().className.replace("\\$.*?$".toRegex(), "").replace(
            '.',
            File.separatorChar
        ) + ".kt"
        return sourcePaths.map { File(it, fileName) }.firstOrNull { it.exists() }
    }

    internal fun getCallerStackTraceElement(): StackTraceElement {
        return Thread.currentThread()
            .stackTrace.first { it.className != javaClass.name && it.className != "java.lang.Thread" && !it.className.contains("KotlinForExample") }
    }

    override fun close() {
    }

    companion object {


        fun markdown(
            block: KotlinForExample.() -> Unit
        ): String {
            val example = KotlinForExample()
            example.use(block)
            return example.buf.toString()
        }

        fun markdownPage(page: Page, block: KotlinForExample.() -> Unit) {

            val nav = listOfNotNull(
                if (!page.previous.isNullOrBlank()) mdLink("previous", page.previous) else null,
                if (!page.parent.isNullOrBlank()) mdLink("parent", page.parent) else null,
                if (!page.next.isNullOrBlank()) mdLink("next", page.next) else null
            )

            val example = KotlinForExample()
            example.use(block)
            val md =
                (if (nav.isNotEmpty()) nav.joinToString(" | ") + "\n---\n\n" else "") +
                """
                    # ${page.title}
                """.trimIndent().trimMargin() + "\n\n" + example.buf.toString() + "\n" +
                (if (nav.isNotEmpty()) "---\n\n" + nav.joinToString(" | ") +"\n\n" else "") +
                        """
                            This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the ${example.mdLinkToSelf("source file")} from which this page is generated.
                        """.trimIndent()

            File(page.outputDir).mkdirs()
            File(page.outputDir, page.fileName).writeText(md)
        }
    }
}
