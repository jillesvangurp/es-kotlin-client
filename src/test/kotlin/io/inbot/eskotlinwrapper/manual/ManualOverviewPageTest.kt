package io.inbot.eskotlinwrapper.manual

import java.io.File
import org.junit.jupiter.api.Test

val readmeMd = "README.md"
val indexMd = "index.md"
val createClientMd = "creating-client.md"
val crudSupportMd = "crud-support.md"
val bulkIndexingMd = "bulk-indexing.md"
val searchMd = "search.md"
val queryDslMd = "query-dsl.md"
val coroutinesMd = "coroutines.md"
val recipeSearchEngineMd = "recipe-search-engine.md"
val aboutThisManualMd = "about.md"

val readmePage = Page("Elasticsearch Kotlin Client", readmeMd, outputDir = ".")
val indexPage = Page("Elasticsearch Kotlin Client Manual", indexMd)
val createClientPage = Page("How to create the client", createClientMd, parent = indexMd, emitBookPage = true)
val crudPage =
    Page("Working with objects", crudSupportMd, parent = indexMd, emitBookPage = true)
val bulkPage = Page("Bulk Indexing", bulkIndexingMd, parent = indexMd, emitBookPage = true)
val searchPage = Page("Search", searchMd, parent = indexMd, emitBookPage = true)
val queryDslPage = Page("Query DSL", queryDslMd, parent = indexMd, emitBookPage = true)
val coroutinesPage = Page("Co-routines", coroutinesMd, parent = indexMd, emitBookPage = true)
val recipeSearchEnginePage = Page("Building a Recipe Search Engine", recipeSearchEngineMd, parent = indexMd, emitBookPage = true)
val aboutThisManualPage = Page("About this manual", aboutThisManualMd, parent = indexMd, emitBookPage = true)

val pages = listOf(createClientPage, crudPage, bulkPage, searchPage, queryDslPage, coroutinesPage, recipeSearchEnginePage, aboutThisManualPage)

class ManualOverviewPageTest {

    @Test
    fun `create ebook create script`() {
        File("epub").mkdirs()
        File("epub", "styles.css").writeText("""
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

        """.trimIndent())
        File("epub", "metadata.yml").writeText("""
            ---
            title: Searching Elasticsearch using Kotlin
            author: Jilles van Gurp
            rights:  Copyright Jilles van Gurp, 2019
            language: en-US
            ...
            
        """.trimIndent())
        File("epub", "preface.md").writeText("""
            # Preface
            
            This Ebook is a work in progress. It may eventually become something I put up on e.g. Amazon for a low fee. 
            Regardless of that, I intend to keep this manual bundled with the source under the MIT License in Markdown
            version.
            
            Meanwhile, I would appreciate any feedback you have on this manual, any grammar/text issues,
            layout/formatting issues, or anything that you would like clarified in more detail.
            
        """.trimIndent())
        File("epub", "create_ebook.sh").writeText("""
            #!/bin/bash
            pandoc --css styles.css -t epub2 -o book.epub -f gfm --metadata-file metadata.yml preface.md ${pages.joinToString(" ") {it.fileName}}
        """.trimIndent())
    }

    @Test
    fun `generate index md`() {
        KotlinForExample.markdownPageWithNavigation(indexPage) {
            +"""
                The [Elasticsearch Kotlin Client](https://github.com/jillesvangurp/es-kotlin-wrapper-client) is a client 
                library written in Kotlin that 
                adapts the [Highlevel Elasticsearch HTTP client for Java](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) provided by Elasticsearch.
                
                ## Chapters
            """
            +pages.joinToString("\n") { "- ${io.inbot.eskotlinwrapper.manual.mdLink(it)}" }
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
    }
}
