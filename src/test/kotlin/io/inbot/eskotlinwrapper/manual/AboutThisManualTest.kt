package io.inbot.eskotlinwrapper.manual

import io.inbot.eskotlinwrapper.AbstractElasticSearchTest
import org.junit.jupiter.api.Test

class AboutThisManualTest : AbstractElasticSearchTest(indexPrefix = "manual") {
    @Test
    fun `about this manual`() {
        KotlinForExample.markdownPageWithNavigation(aboutThisManualPage) {
            +"""
                ## About this manual
                
                This manual is an attempt at 
                ${mdLink("literate programming","https://en.wikipedia.org/wiki/Literate_programming")}.
                 
                The idea for this is simple: 
                 
                 - Kotlin has multi line strings where we can embed markdown. So, mixing code and markdown is easy.
                 - Manuals need code examples and manually copying code examples to Markdown files sucks because no refactoring, no compilation,
                    type checking, no verifying the example still works, etc.
                 - Kotlin has very nice support for internal DSLs.
                 - So, use code blocks as an example and look up the source code to generate markdown containing the
                lines of that block and put that code in normal tests so that running the tests generates Github
                flavored markdown that I commit along with the rest of the code and make sure my tests break
                when any of the examples no longer compiles/works. Win Win!
                 - Also capture output of blocks and show that in the documentation.
                 
                In short, that's what the ${mdLink(KotlinForExample::class)} class does. This grew out of my frustration with 
                keeping the documentation in sync with the rapidly changing implementation of the Elasticsearch Kotlin Client.
                
                Eventually, this will move to a separate library once I'm happy enough 
                with how it works. Currently, I'm using this to generate the manual for this project and experimenting
                a little with this.
                
                And obviously that project will eat its own dogfood to produce a more detailed manual on how to use it self. Here's
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
                        Helpers for creating links links are provided:
                        
                        - You can link to another page ${mdLink(indexPage)}
                        - You can link to the source of any class ${mdLink(KotlinForExample::class)}
                    """
                }
            }

            +"""
                ## Epub support.
                
                I'm also thinking about eventually publishing an ebook version of the manual at some point. The build 
                currently produces scripts in the epub directory that allow you to create an epub using pandoc. This 
                is very much a work in progress but was a big motivation for me to sink time in creating this 
                little literate programming framework.
                
                You can find the latest epub
                [here](https://github.com/jillesvangurp/es-kotlin-wrapper-client/blob/master/book.epub).
            """
        }
    }
}
