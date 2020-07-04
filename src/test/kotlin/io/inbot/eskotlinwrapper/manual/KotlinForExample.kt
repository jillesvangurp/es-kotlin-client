package io.inbot.eskotlinwrapper.manual

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.kotlin4example.mdLink
import mu.KLogger
import mu.KotlinLogging
import java.io.File

private val logger: KLogger = KotlinLogging.logger { }

val sourceRepository = SourceRepository(
    repoUrl = "https://github.com/jillesvangurp/es-kotlin-wrapper-client",
    sourcePaths = setOf("src/main/kotlin", "src/test/kotlin", "src/examples/kotlin")
)

fun markdownPageWithNavigation(page: Page, markdown: String) {
    val index = pages.indexOf(page)
    val previous = if (index < 0) null else if (index == 0) null else pages[index - 1].fileName
    val next = if (index < 0) null else if (index == pages.size - 1) null else pages[index + 1].fileName
    val nav = listOfNotNull(
        if (!previous.isNullOrBlank()) mdLink("previous", previous) else null,
        if (!page.parent.isNullOrBlank()) mdLink("index", page.parent) else null,
        if (!next.isNullOrBlank()) mdLink("next", next) else null
    )

    val md =
        """
                # ${page.title}
                
        """.trimIndent().trimMargin() + "\n\n" + markdown

    val pageWithNavigationMd =
        (if (nav.isNotEmpty()) nav.joinToString(" | ") + "\n\n___\n\n" else "") +
            md + "\n" +
            (if (nav.isNotEmpty()) "___\n\n" + nav.joinToString(" | ") + "\n\n" else "")

    File(page.outputDir).mkdirs()
    File(page.outputDir, page.fileName).writeText(pageWithNavigationMd)
    if (page.emitBookPage) {
        File("epub").mkdirs()
        File("epub", page.fileName).writeText(md)
    }
}

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
