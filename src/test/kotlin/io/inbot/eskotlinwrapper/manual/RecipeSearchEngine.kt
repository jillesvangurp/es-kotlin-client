package io.inbot.eskotlinwrapper.manual

import org.junit.jupiter.api.Test
import java.io.File

class RecipeSearchEngine {

    @Test
    fun `article markdown`() {
        KotlinForExample.markdownPageWithNavigation(recipeSearchEnginePage) {
            sourcePaths += "src/examples/kotlin"
            +"""
                    The Elastic Search Kotlin Wrapper is designed to simplify writing production code that
                    interacts with Elasticsearch.
                    
                    The easiest way to demonstrate how it works is just showing it with a simple 
                    example. The code below is very loosely based on an example in the 
                    [Elastic examples repository](https://github.com/elastic/examples/tree/master/Search/recipe_search_java) 
                    and borrows data from that project.

                    In this article, we will create a simple KTor server that 
                    implements a simple Rest service for indexing and searching recipes.
                    To make things interesting, we'll use co-routines, which just means that KTor uses asynchronous 
                    communication and can scale really well without using a lot of threads.
                    
                    You can find the source code for this example 
                    ${mdLinkToRepoResource("here","src/examples/kotlin/recipesearch")}.

                    ## Our data model
                    
                    Lets start with our data model and consider this simple example json file for chicken enchilladas:
                    
                """
            mdCodeBlock(File("src/examples/resources/recipes/homemade-chicken-enchiladas.json").readText(), "json", allowLongLines = true)
            +"""
                    We can create a simple Kotlin data model to represent recipes like this:
                """
            snippetFromSourceFile("src/examples/kotlin/recipesearch/Recipe.kt", "model classes")

            +"""
                    Given this model, we can create simple `AsyncIndexRepository` and use it (see ${mdLink(crudPage)}) 
                    to create a simple ktor server. Lets start with our `main` function
            """
            snippetFromSourceFile("src/examples/kotlin/recipesearch/ServerMain.kt", "main_function")

            +"""
                This creates an Elasticsearch client, a jackson object mapper, which we will use for serialization, 
                and an `AsyncIndexRepository`, which is version of the IndexRepository that can use co-routines. 

                These objects are injected into a `RecipeSearch` instance that contains
                our business logic. Finally, we pass that instance to a method that constructs a simple asynchronous 
                KTor server.
                
                Note that our `main` function is marked as `suspend`. This means our KTor server creates a co-routine 
                for each request. And since we used `AsyncIndexRepository` instead of the `IndexRepository`, everything
                is asynchronous.
                
                ## The business logic
                
                First we need to be able to index recipe documents. We do this with a simple function that uses the
                bulk DSL to bulk index all the files in the `src/examples/resources/recipes` directory.
            """
            snippetFromSourceFile("src/examples/kotlin/recipesearch/RecipeSearch.kt", "index_recipes")

            +"""
                Note how small this code is. There's almost nothing to this. Yet this code is safe, robust, asynchronous,
                and it could trivially be modified to process many millions of documents. Simply set a larger bulk size 
                and iterate over a bigger data source. It doesn't matter where the data comes from. You could iterate
                over a database table, a CSV file, crawl the web, etc.
                
                Once we have documents in our index, we can search through them as follows:
            """
            snippetFromSourceFile("src/examples/kotlin/recipesearch/RecipeSearch.kt", "search_recipes")

            +"""
                As you can see, searching is similarly simple. The `search` extension function takes a block that
                allows you to customise a `SearchRequest`. Inside the block we set the `size` and `from` so we can 
                page through multiple pages of results.
                
                Then hardest part is adding a query. For this the client provides several options. In this case,
                we use Kotlin's `apply` extension function to make dealing with the Java builders in the `RestHighLevelClient`
                a bit more idiomatic. The advantage of this is that we don't have to chain the builder methods and gain some 
                compile time safety. We could also have opted to use a templated multi-line string as the source.
                
                ## Creating a Ktor server
                
                To expose the business logic via a simple REST service, we use KTor. Note that recent versions of
                Spring Boot also support co-routines so you may be able to adapt this example for use with that.
            """
            snippetFromSourceFile("src/examples/kotlin/recipesearch/ServerMain.kt", "ktor_setup")

            """
                KTor use a DSL for defining the server. In this case, we simply reuse our Jackson object mapper
                to setup content negotiation and data conversion and then add a router with a few simple endpoints.
                
                Note tha
            """


        }
    }
}
