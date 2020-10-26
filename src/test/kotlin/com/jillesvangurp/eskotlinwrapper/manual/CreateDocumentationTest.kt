package com.jillesvangurp.eskotlinwrapper.manual

import com.jillesvangurp.kotlin4example.SourceRepository
import com.jillesvangurp.kotlin4example.mdLink
import org.junit.jupiter.api.Test
import java.io.File

const val manualIndexMd = "index.md"

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
val sourceGitRepository = SourceRepository(
    repoUrl = "https://github.com/jillesvangurp/es-kotlin-wrapper-client",
    sourcePaths = setOf("src/main/kotlin", "src/test/kotlin", "src/examples/kotlin")
)

val readmePage = Page("Elasticsearch Kotlin Client", "README.md", outputDir = ".")
val manualIndexPage = Page("Elasticsearch Kotlin Client Manual", manualIndexMd)
val createClientPage = Page("How to create the client", "creating-client.md", parent = manualIndexMd, emitBookPage = true)
val indexRepositoryPage =
    Page("Working with objects", "crud-support.md", parent = manualIndexMd, emitBookPage = true)
val bulkPage = Page("Bulk Indexing", "bulk-indexing.md", parent = manualIndexMd, emitBookPage = true)
val searchPage = Page("Search", "search.md", parent = manualIndexMd, emitBookPage = true)
val queryDslPage = Page("Query DSL", "query-dsl.md", parent = manualIndexMd, emitBookPage = true)
val coroutinesPage = Page("Co-routines", "coroutines.md", parent = manualIndexMd, emitBookPage = true)
val recipeSearchEnginePage = Page(
    "Building a Recipe Search Engine",
    "recipe-search-engine.md", parent = manualIndexMd, emitBookPage = true
)
val aboutThisManualPage = Page("About this manual", "about.md", parent = manualIndexMd, emitBookPage = true)

val manualPages = listOf(createClientPage, indexRepositoryPage, bulkPage, searchPage, queryDslPage, coroutinesPage, recipeSearchEnginePage, aboutThisManualPage)

class ManualOverviewPageTest {
    @Test
    fun `generate index md`() {
        val markdown by sourceGitRepository.md {
            +"""
                The [Elasticsearch Kotlin Client](https://github.com/jillesvangurp/es-kotlin-wrapper-client) is a client 
                library written in Kotlin that 
                adapts the [Highlevel Elasticsearch HTTP client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) provided by Elasticsearch.
                
                ## Chapters
            """
            +manualPages.joinToString("\n") { "- ${mdLink(it.title, it.fileName)}" }
            +"""
                
                ## Introduction
                
                The official Java client provides client functionality for essentially everything exposed by their REST
                API. The Elasticsearch Kotlin Client makes using this functionality more Kotlin friendly. 

                It does this
                through extension functions that add many useful features and shortcuts. It adds Kotlin DSLs for
                querying, defining mappings, and bulk indexing. To facilitate the most common use cases, this library
                also provides a Repository abstraction that enables the user to interact with an index in a way that
                is less boilerplate heavy.
                
                Additionally, it provides co-routine friendly versions of the asynchronous clients in the Java library.
                This enables the user to write fully reactive code in e.g. Ktor or Spring Boot. This makes this
                library the easiest way to do this currently.
            """
        }
        markdownPageWithNavigation(manualIndexPage, markdown)
    }

    @Test
    fun `generate manual`() {
        mapOf(
            readmePage to readme,
            aboutThisManualPage to about,
            createClientPage to clientCreation,
            indexRepositoryPage to indexRepository,
            bulkPage to bulk,
            searchPage to search,
            coroutinesPage to coRoutines,
            queryDslPage to queryDsl,
            recipeSearchEnginePage to recipeSearch
        ).forEach { (page, md) ->
            markdownPageWithNavigation(page, md)
        }
    }

    @Test
    fun `create ebook create script`() {
        File("epub").mkdirs()
        File("epub", "styles.css").writeText(
            """
            /* This defines styles and classes used in the book */
            body { margin: 0; text-align: justify; font-size: medium; font-family: Athelas, Georgia, serif; }
            h1 { text-align: left; }
            h2 { text-align: left; }
            h3 { text-align: left; }
            h4 { text-align: left; }
            h5 { text-align: left; }
            h6 { text-align: left; }
            h1.title { }
            h2.author { }
            h3.date { }
            nav#toc ol,
            nav#landmarks ol { padding: 0; margin-left: 1em; }
            nav#toc ol li,
            nav#landmarks ol li { list-style-type: none; margin: 0; padding: 0; }
            a.footnote-ref { vertical-align: super; }
            em, em em em, em em em em em { font-style: italic;}
            em em, em em em em { font-style: normal; }
            pre {font-size: 50%;}
            code{ font-family: monospace; white-space: pre-wrap; }
            span.smallcaps{ font-variant: small-caps; }
            span.underline{ text-decoration: underline; }
            q { quotes: "“" "”" "‘" "’"; }
            div.column{ display: inline-block; vertical-align: top; width: 50%; }
            div.hanging-indent{margin-left: 1.5em; text-indent: -1.5em;}
            
            /* https://pandoc.org/demos.html */
            code span.al { color: #ff0000; } /* Alert */
            code span.an { color: #008000; } /* Annotation */
            code span.at { } /* Attribute */
            code span.bu { } /* BuiltIn */
            code span.cf { color: #0000ff; } /* ControlFlow */
            code span.ch { color: #008080; } /* Char */
            code span.cn { } /* Constant */
            code span.co { color: #008000; } /* Comment */
            code span.cv { color: #008000; } /* CommentVar */
            code span.do { color: #008000; } /* Documentation */
            code span.er { color: #ff0000; font-weight: bold; } /* Error */
            code span.ex { } /* Extension */
            code span.im { } /* Import */
            code span.in { color: #008000; } /* Information */
            code span.kw { color: #0000ff; } /* Keyword */
            code span.op { } /* Operator */
            code span.ot { color: #ff4000; } /* Other */
            code span.pp { color: #ff4000; } /* Preprocessor */
            code span.sc { color: #008080; } /* SpecialChar */
            code span.ss { color: #008080; } /* SpecialString */
            code span.st { color: #008080; } /* String */
            code span.va { } /* Variable */
            code span.vs { color: #008080; } /* VerbatimString */
            code span.wa { color: #008000; font-weight: bold; } /* Warning */

            """.trimIndent()
        )
        File("epub", "metadata.yml").writeText(
            """
            ---
            title: Searching Elasticsearch using Kotlin
            author: Jilles van Gurp
            rights:  Copyright Jilles van Gurp, 2019
            language: en-US
            ...
            
            """.trimIndent()
        )
        File("epub", "preface.md").writeText(
            """
            # Preface
            
            This Ebook is a work in progress. It may eventually become something I put up on e.g. Amazon for a low fee. 
            Regardless of that, I intend to keep this manual bundled with the source under the MIT License in Markdown
            version.
            
            Meanwhile, I would appreciate any feedback you have on this manual, any grammar/text issues,
            layout/formatting issues, or anything that you would like clarified in more detail.
            
            """.trimIndent()
        )
        File("epub", "create_ebook.sh").writeText(
            """
            #!/bin/bash
            pandoc --css styles.css -t epub2 -o book.epub -f gfm --metadata-file metadata.yml preface.md ${manualPages.joinToString(" ") {it.fileName}}
            """.trimIndent()
        )
    }
}

fun markdownPageWithNavigation(page: Page, markdown: String) {
    val index = manualPages.indexOf(page)
    val previous = if (index < 0) null else if (index == 0) null else manualPages[index - 1].fileName
    val next = if (index < 0) null else if (index == manualPages.size - 1) null else manualPages[index + 1].fileName
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
