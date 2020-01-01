package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test
import java.io.File

val readmeMd = "README.md"
val indexMd = "index.md"
val createClientMd = "creating-client.md"
val crudSupportMd = "crud-support.md"
val bulkIndexingMd = "bulk-indexing.md"
val searchMd = "search.md"
val queryDslMd = "query-dsl.md"
val coroutinesMd = "coroutines.md"


val readmePage = Page("Es Kotlin Wrapper Client", readmeMd, outputDir = ".")
val indexPage = Page("Es Kotlin Wrapper Client Manual", indexMd)
val createClientPage = Page("How to create the client", createClientMd, parent = indexMd)
val crudPage =
    Page("Working with objects", crudSupportMd, parent = indexMd)
val bulkPage = Page("Bulk Indexing", bulkIndexingMd, parent = indexMd)
val searchPage = Page("Search", searchMd, parent = indexMd)
val queryDslPage = Page("Query DSL", queryDslMd, parent = indexMd)
val coroutinesPage = Page("Co-routines", coroutinesMd, parent = indexMd)

val pages=listOf(createClientPage,crudPage,bulkPage,searchPage,queryDslPage,coroutinesPage)

class ManualOverviewPageTest {

    @Test
    fun `create ebook create script`() {
        File("epub","styles.css").writeText("""
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
            pre {font-size: 60%;}
            code{ font-family: monospace; white-space: pre-wrap; }
            span.smallcaps{ font-variant: small-caps; }
            span.underline{ text-decoration: underline; }
            q { quotes: "“" "”" "‘" "’"; }
            div.column{ display: inline-block; vertical-align: top; width: 50%; }
            div.hanging-indent{margin-left: 1.5em; text-indent: -1.5em;}

        """.trimIndent())
        File("epub","title.txt").writeText("""
            ---
            title: Searching Elasticsearch using Kotlin
            author: Jilles van Gurp
            rights:  Copyright Jilles van Gurp, 2019
            language: en-US
        """.trimIndent())
        File("epub","create_ebook.sh").writeText("""
            #!/bin/bash
            pandoc --css styles.css -t epub2 -o book.epub -f gfm title.txt ${pages.joinToString (" ") {it.fileName}}
        """.trimIndent())
    }

    @Test
    fun `generate index md`() {
        KotlinForExample.markdownPage(indexPage) {
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
                I'd love your feedback on how this works.
                 
                The idea for this is extremely simple:
                 
                 - Kotlin has multi line strings where we can embed markdown
                 - Manuals need code examples
                 - Manually copying code to Markdown files sucks because no refactoring, no compilation, 
                 no verifying the example still works, etc
                 - Kotlin has nice support for internal DSLs
                 - Idea: use codeblocks as an example and look up the source code to generate markdown containing the lines of that block
                 - Put that code in normal tests so that running the tests generates Github flavored markdown that I commit
                 along with the rest of the code.
                 
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
                KotlinForExample.markdownPage(demoPage) {
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
        }
    }
}
