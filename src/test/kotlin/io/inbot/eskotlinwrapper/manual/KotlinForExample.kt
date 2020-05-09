package io.inbot.eskotlinwrapper.manual

import mu.KLogger
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintWriter
import kotlin.reflect.KClass

private val logger: KLogger = KotlinLogging.logger { }

fun mdLink(title: String, target: String) = "[$title]($target)"

/**
 * Simple abstraction for a page. Pages go in some output directory, have a title, and may or may not be part of a book.
 */
data class Page(
    val title: String,
    val fileName: String,
    val outputDir: String = "manual",
    val parent: String? = null,
    val emitBookPage: Boolean = false
)

fun mdLink(page: Page) = mdLink(page.title, page.fileName)

@Suppress("MemberVisibilityCanBePrivate")
class KotlinForExample private constructor(
    val sourcePaths: MutableSet<String> = mutableSetOf("src/main/kotlin", "src/test/kotlin"),
    private val repoUrl: String = "https://github.com/jillesvangurp/es-kotlin-wrapper-client"
) : AutoCloseable {
    private val buf = StringBuilder()
    private val patternForBlock = "block.*?\\{+".toRegex(RegexOption.MULTILINE)

    operator fun String.unaryPlus() {
        buf.appendln(this.trimIndent().trimMargin())
        buf.appendln()
    }

    fun mdCodeBlock(
        code: String,
        type: String = "kotlin",
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80
    ) {
        var c = code.replace("    ", "  ")
        if (wrap) {
            c = c.lines().flatMap { line ->
                if (line.length <= lineLength) {
                    listOf<String>(line)
                } else {
                    line.chunked(lineLength)
                }
            }.joinToString("\n")
        }
        if (!allowLongLines && c.lines().firstOrNull { it.length > lineLength } != null) {
            logger.warn { "code block contains lines longer than 80 characters\n${(1 until lineLength).joinToString("") { "." } + "|"}\n$c" }
            throw IllegalArgumentException("code block exceeds line length of ")
        }

        buf.appendln("```$type\n$c\n```\n")
    }

    fun mdLink(clazz: KClass<*>): String {
        return mdLink(
            "`${clazz.simpleName!!}`",
            "$repoUrl/tree/master/${sourcePathForClass(clazz)}"
        )
    }

    fun mdLinkToRepoResource(title: String, relativeUrl: String, branch: String = "master") =
        mdLink(title, "$repoUrl/tree/$branch/$relativeUrl")

    fun snippetBlockFromClass(clazz: KClass<*>, snippetId: String) {
        val fileName = sourcePathForClass(clazz)
        snippetFromSourceFile(fileName, snippetId)
    }

    fun snippetFromSourceFile(
        fileName: String,
        snippetId: String,
        allowLongLines: Boolean = false,
        wrap: Boolean = false,
        lineLength: Int = 80
    ) {
        val snippetLines = mutableListOf<String>()
        val lines = File(fileName).readLines()
        var inSnippet = false
        for (line in lines) {
            if (inSnippet && line.contains(snippetId)) {
                break // break out of the loop
            }
            if (inSnippet) {
                snippetLines.add(line)
            }

            if (!inSnippet && line.contains(snippetId)) {
                inSnippet = true
            }
        }
        if (snippetLines.size == 0) {
            throw IllegalArgumentException("Snippet $snippetId not found in $fileName")
        }
        mdCodeBlock(
            snippetLines.joinToString("\n").trimIndent(),
            allowLongLines = allowLongLines,
            wrap = wrap,
            lineLength = lineLength
        )
    }

    private fun sourcePathForClass(clazz: KClass<*>) =
        sourcePaths.map { File(it, fileName(clazz)) }.first { it.exists() }.path

    private fun fileName(clazz: KClass<*>) =
        clazz.qualifiedName!!.replace("\\$.*?$".toRegex(), "").replace('.', File.separatorChar) + ".kt"

    fun mdLinkToSelf(title: String = "Link to this source file"): String {
        val fn = this.sourceFileOfExampleCaller() ?: throw IllegalStateException("source file not found")
        return mdLink(title, "$repoUrl/tree/master/${fn.path}")
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

    fun blockWithOutput(
        allowLongLines: Boolean = false,
        allowLongLinesInOutput: Boolean = false,
        wrap: Boolean = false,
        wrapOutput: Boolean = false,
        lineLength: Int = 80,
        block: PrintWriter.() -> Unit
    ) {
        val callerSourceBlock = getCallerSourceBlock()

        val outputBuffer = ByteArrayOutputStream()
        val writer = PrintWriter(outputBuffer.writer())
        writer.use {
            block.invoke(writer)
            if (callerSourceBlock == null) {
                logger.warn { "Could not find code block from stack trace and sourcePath" }
            } else {
                mdCodeBlock(
                    code = callerSourceBlock,
                    allowLongLines = allowLongLines,
                    lineLength = lineLength,
                    wrap = wrap
                )
            }
            writer.flush()
        }
        val output = outputBuffer.toString()
        if (output.isNotEmpty()) {
            buf.appendln("Output:\n")
            mdCodeBlock(
                code = output,
                allowLongLines = allowLongLinesInOutput,
                wrap = wrapOutput,
                lineLength = lineLength,
                type = ""
            )
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
        logger.warn("no suitable file found for ${ste.fileName} ${ste.lineNumber}")
        return null
    }

    internal fun sourceFileOfExampleCaller(): File? {
        val fileName = getCallerStackTraceElement().className.replace("\\$.*?$".toRegex(), "").replace(
            '.',
            File.separatorChar
        ) + ".kt"
        return sourcePaths.map { File(it, fileName) }.firstOrNull { it.exists() }
    }

    internal fun getCallerStackTraceElement(): StackTraceElement {
        return Thread.currentThread()
            .stackTrace.first {
                !it.className.startsWith("java") &&
                        !it.className.startsWith("jdk.internal") &&
                        it.className != javaClass.name &&
                        it.className != "java.lang.Thread" &&
                        it.className != "io.inbot.eskotlinwrapper.manual.KotlinForExample" &&
                        it.className != "io.inbot.eskotlinwrapper.manual.KotlinForExample\$Companion" // edge case


            }
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

        fun markdownPageWithNavigation(page: Page, block: KotlinForExample.() -> Unit) {
            val index = pages.indexOf(page)
            val previous = if (index < 0) null else if (index == 0) null else pages[index - 1].fileName
            val next = if (index < 0) null else if (index == pages.size - 1) null else pages[index + 1].fileName
            val nav = listOfNotNull(
                if (!previous.isNullOrBlank()) mdLink("previous", previous) else null,
                if (!page.parent.isNullOrBlank()) mdLink("parent", page.parent) else null,
                if (!next.isNullOrBlank()) mdLink("next", next) else null
            )

            val example = KotlinForExample()

            example.use(block)
            val md = """
                # ${page.title}
            """.trimIndent().trimMargin() + "\n\n" + example.buf.toString()

            val pageWithNavigationMd =
                (if (nav.isNotEmpty()) nav.joinToString(" | ") + "\n---\n\n" else "") +
                        md + "\n" +
                        (if (nav.isNotEmpty()) "---\n\n" + nav.joinToString(" | ") + "\n\n" else "") +
                        """
                            This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the ${example.mdLinkToSelf(
                            "source file"
                        )} from which this page is generated.
                        """.trimIndent()

            File(page.outputDir).mkdirs()
            File(page.outputDir, page.fileName).writeText(pageWithNavigationMd)
            if (page.emitBookPage) {
                File("epub").mkdirs()
                File("epub", page.fileName).writeText(md)
            }
        }
    }
}
