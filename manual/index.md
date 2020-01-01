# Es Kotlin Wrapper Client Manual

The Elasticsearch Kotlin Wrapper Client adds a lot of functionality to the 
Elasticsearch Rest High Level Client. This is mostly done through extension functions.

In this manual, we introduce the features through examples.

- [How to create the client](creating-client.md)
- [Working with objects](crud-support.md)
- [Bulk Indexing](bulk-indexing.md)
- [Search](search.md)
- [Query DSL](query-dsl.md)
- [Co-routines](coroutines.md)

## About this manual

This manual is my attempt at 
[literate programming](https://en.wikipedia.org/wiki/Literate_programming).
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
 
In short, that's what [`KotlinForExample`](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/KotlinForExample.kt) does.

This will probably move to a separate library when I'm happy enough 
with how it works. Currently, I'm using this to generate the manual for this project and experimenting
a little with this.

And obviously that project will eat its own dogfood to produce a more detailed manual for that. Here's
a sneak preview of that.

```kotlin
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
```


This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/ManualOverviewPageTest.kt) from which this page is generated.