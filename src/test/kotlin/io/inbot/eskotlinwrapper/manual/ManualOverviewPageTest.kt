package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test

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
val createClientPage = Page("How to create the client", createClientMd, parent = indexMd, next = crudSupportMd)
val crudPage =
    Page("Working with objects", crudSupportMd, parent = indexMd, next = bulkIndexingMd, previous = createClientMd)
val bulkPage = Page("Bulk Indexing", bulkIndexingMd, parent = indexMd, next = searchMd, previous = crudSupportMd)
val searchPage = Page("Search", searchMd, parent = indexMd, previous = bulkIndexingMd, next = queryDslMd)
val queryDslPage = Page("Query DSL", queryDslMd, parent = indexMd, previous = searchMd, next = coroutinesMd)
val coroutinesPage = Page("Co-routines", coroutinesMd, parent = indexMd, previous = queryDslMd)

class ManualOverviewPageTest {

    @Test
    fun `generate index md`() {
        KotlinForExample.markdownPage(indexPage) {
            +"""
                The Elasticsearch Kotlin Wrapper Client adds a lot of functionality to the 
                Elasticsearch Rest High Level Client. This is mostly done through extension functions.
                
                In this manual, we introduce the features through examples.
                
                - ${mdLink(createClientPage)}
                - ${mdLink(crudPage)}
                - ${mdLink(bulkPage)}
                - ${mdLink(searchPage)}
                - ${mdLink(queryDslPage)}
                - ${mdLink(coroutinesPage)}
                
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
                        Once this code runs, it actually generates the page [you can open it here](demo.md)
                    """
                    blockWithOutput {
                        // Block receives a PrintWriter so the output of the block is captured and shown.
                        println("Hello world")
                    }
                    +"""
                        We use a few kotlin helpers for links:
                        
                        - You can link to another page ${mdLink(indexPage)} (gets you back to the manual page if you
                        clicked the demo.md link).
                        - You can link to the source of any class ${mdLink(KotlinForExample::class)}
                    """
                }
            }

        }
    }
}
