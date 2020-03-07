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

val readmePage = Page("Es Kotlin Wrapper Client", readmeMd, outputDir = ".")
val indexPage = Page("Es Kotlin Wrapper Client Manual", indexMd)
val createClientPage = Page("How to create the client", createClientMd, parent = indexMd, emitBookPage = true)
val crudPage =
    Page("Working with objects", crudSupportMd, parent = indexMd, emitBookPage = true)
val bulkPage = Page("Bulk Indexing", bulkIndexingMd, parent = indexMd, emitBookPage = true)
val searchPage = Page("Search", searchMd, parent = indexMd, emitBookPage = true)
val queryDslPage = Page("Query DSL", queryDslMd, parent = indexMd, emitBookPage = true)
val coroutinesPage = Page("Co-routines", coroutinesMd, parent = indexMd, emitBookPage = true)
val recipeSearchEnginePage = Page("Building a Recipe Search Engine", recipeSearchEngineMd, parent = indexMd, emitBookPage = true)

val pages = listOf(createClientPage, crudPage, bulkPage, searchPage, queryDslPage, coroutinesPage, recipeSearchEnginePage)

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
            
            This Ebook is a work in progress. It may eventually be something I put up on e.g. Amazon for a low fee. 
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
                The Elasticsearch Kotlin Wrapper Client adds a lot of functionality to the 
                Elasticsearch Rest High Level Client. This is mostly done through extension functions.
                
                In this manual, we introduce the features through examples.
            """
            +pages.joinToString("\n") { "- ${mdLink(it)}" }
            +"""
                ## About this manual
                
                This manual is my attempt at 
                ${mdLink("literate programming","https://en.wikipedia.org/wiki/Literate_programming")}.
                **I'd love your feedback on how this works**.
                 
                The idea for this is extremely simple:
                 
                 - Kotlin has multi line strings where we can embed markdown
                 - Manuals need code examples
                 - Manually copying code to Markdown files sucks because no refactoring, no compilation, type checking, 
                 no verifying the example still works, etc.
                 - Kotlin has very nice support for internal DSLs
                 - Idea: use codeblocks as an example and look up the source code to generate markdown containing the lines of that block
                 - Put that code in normal tests so that running the tests generates Github flavored markdown that I commit
                 along with the rest of the code and make sure my tests break when any of the examples no 
                longer compiles/works. Win Win!
                 
                In short, that's what ${mdLink(KotlinForExample::class)} does.
                
                This will probably move to a separate library when I'm happy enough 
                with how it works. Currently, I'm using this to generate the manual for this project and experimenting
                a little with this.
                
                And obviously that project will eat its own dogfood to produce a more detailed manual for that. Here's
                a sneak preview of that.
            """

            // very meta
            blockWithOutput {
                val demoPage = Page("Demo Page", "demo.md")
                KotlinForExample.markdownPageWithNavigation(demoPage) {
                    +"""
                        A quick example. You can put any markdown here.
                        Once this code runs, it actually generates the page: [open it here](demo.md)
                    """
                    blockWithOutput {
                        // Block receives a PrintWrite, the output of the block is captured.
                        println("Hello world")
                    }
                    +"""
                        We use a few kotlin helpers for links:
                        
                        - You can link to another page ${mdLink(indexPage)}
                        - You can link to the source of any class ${mdLink(KotlinForExample::class)}
                    """
                }
            }

            +"""
                ## Epub support.
                
                I'm also thinking about producing an ebook version of the manual at some point and the build 
                currently produces scripts in the epub directory that allow you to create an epub using pandoc. This 
                is very much a work in progress but was a big motivation for me to sink time in creating this 
                little literate programming framework.
            """
        }
    }
}
