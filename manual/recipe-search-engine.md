[previous](coroutines.md) | [parent](index.md)
---

# Building a Recipe Search Engine

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
[here](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/examples/kotlin/recipesearch).

## Our data model

Lets start with our data model and consider this simple example json file for chicken enchilladas:

```json
{
	"title": "Homemade Chicken Enchiladas",
	"description": "These enchiladas are great. Even my 5 year old loves them!",
	"ingredients": [
		"1 tablespoon olive oil",
		"2 cooked chicken breasts, shredded",
		"1 onion, diced",
		"1 green bell pepper, diced",
		"1 1/2 cloves garlic, chopped",
		"1 cup cream cheese",
		"1 cup shredded Monterey Jack cheese",
		"1 (15 ounce) can tomato sauce",
		"1 tablespoon chili powder",
		"1 tablespoon dried parsley",
		"1 teaspoon dried oregano",
		"1/2 teaspoon salt",
		"1/2 teaspoon ground black pepper",
		"8 (10 inch) flour tortillas",
		"2 cups enchilada sauce",
		"1 cup shredded Monterey Jack cheese"
	],
	"directions": [
		"Preheat oven to 350 degrees F (175 degrees C).",
		"Heat olive oil in a skillet over medium heat. Cook and stir chicken, onion, green bell pepper, garlic, cream cheese, and 1 cup Monterey Jack cheese in hot oil until the cheese melts, about 5 minutes. Stir tomato sauce, chili powder, parsley, oregano, salt, and black pepper into the chicken mixture.",
		"Divide mixture evenly into tortillas, roll the tortillas around the filling, and arrange in a baking dish. Cover with enchilada sauce and remaining 1 cup Monterey Jack cheese.",
		"Bake in preheated oven until cheese topping melts and begins to brown, about 15 minutes."
	],
	"prep_time_min": 15,
	"cook_time_min": 20,
	"servings": 8,
	"tags": [ "main dish" ],
	"author": {
		"name": "Mary Kate",
		"url": "http://allrecipes.com/cook/14977239/profile.aspx"
	},
	"source_url": "http://allrecipes.com/Recipe/Homemade-Chicken-Enchiladas/Detail.aspx"
}

```

We can create a simple Kotlin data model to represent recipes like this:

```kotlin
data class Author(val name: String, val url: String)

data class Recipe(
  val title: String,
  val description: String,
  val ingredients: List<String>,
  val directions: List<String>,
  val prepTimeMin: Int,
  val cookTimeMin: Int,
  val servings: Int,
  val tags: List<String>,
  val author: Author,
  // we will use this as our ID as well
  val sourceUrl: String
)
```

Given this model, we can create simple `AsyncIndexRepository` and use it (see [Working with objects](crud-support.md)) 
to create a simple ktor server. Lets start with our `main` function

```kotlin
@ExperimentalCoroutinesApi
suspend fun main(vararg args: String) {
  val objectMapper = ObjectMapper()
  // enable Kotlin integration and whatever else is on the classpath
  objectMapper.findAndRegisterModules()
  // make sure we convert names with underscores properly to and
  // from kotlin (camelCase)
  objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

  val esClient = create(host = "localhost", port = 9200)
  // shut down client cleanly after ktor exits
  esClient.use {
    val recipeRepository =
      esClient.asyncIndexRepository<Recipe>(index = "recipes")
    val recipeSearch = RecipeSearch(recipeRepository, objectMapper)
    if(args.any { it == "-c" }) {
      // if you pass -c it bootstraps an index
      recipeSearch.deleteIndex()
      recipeSearch.createNewIndex()
      recipeSearch.indexExamples()
    }

    // creates a simple ktor server
    createServer(objectMapper, recipeSearch).start(wait = true)
  }
}
```

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

```kotlin
suspend fun indexExamples() {
  // use a small bulk size to illustrate how this can
  // work with potentially large amounts of files.
  recipeRepository.bulk(bulkSize = 3) {
    File("src/examples/resources/recipes")
      .listFiles { f -> f.extension == "json" }?.forEach {
        val parsed = objectMapper.readValue<Recipe>(it.readText())
        // lets use the sourceUrl as an id
        // use create=false to allow updates
        index(parsed.sourceUrl, parsed, create = false)
      }
  }
}
```

Note how small this code is. There's almost nothing to this. Yet this code is safe, robust, asynchronous,
and it could trivially be modified to process many millions of documents. Simply set a larger bulk size 
and iterate over a bigger data source. It doesn't matter where the data comes from. You could iterate
over a database table, a CSV file, crawl the web, etc.

Once we have documents in our index, we can search through them as follows:

```kotlin
suspend fun search(query: String, from: Int, size: Int):
    SearchResponse<Recipe> {
  return recipeRepository.search {
    source(SearchSourceBuilder.searchSource().apply {
      from(from)
      size(size)
      query(
        QueryBuilders.boolQuery().apply {
          should().apply {
            add(QueryBuilders.matchPhraseQuery("title", query).boost(2.0f))
            add(QueryBuilders.matchQuery("title", query).boost(2.0f))
            add(QueryBuilders.matchQuery("description", query))
          }
        }
      )
    })
  }.toSearchResponse()
}
```

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

```kotlin
private fun createServer(
  objectMapper: ObjectMapper,
  recipeSearch: RecipeSearch
): NettyApplicationEngine {
  return embeddedServer(Netty, port = 8080) {
    // this will allow us to serialize data objects to json
    install(DataConversion)
    install(ContentNegotiation) {
      // lets reuse our mapper for this
      register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }

    routing {
      get("/") {
        call.respondText("Hello World!", ContentType.Text.Plain)
      }
      post("/recipe_index") {
        recipeSearch.createNewIndex()
        call.respond(HttpStatusCode.Created)
      }

      delete("/recipe_index") {
        recipeSearch.deleteIndex()
        call.respond(HttpStatusCode.Gone)
      }

      post("/index_examples") {
        recipeSearch.indexExamples()
        call.respond(HttpStatusCode.Accepted)
      }

      get("/health") {
        val healthStatus = recipeSearch.healthStatus()
        if (healthStatus == ClusterHealthStatus.RED) {
          call.respond(
            HttpStatusCode.ServiceUnavailable,
            "es cluster is $healthStatus")
        } else {
          call.respond(
            HttpStatusCode.OK,
            "es cluster is $healthStatus")
        }
      }

      get("/search") {
        val params = call.request.queryParameters
        val query = params["q"].orEmpty()
        val from = params["from"]?.toInt() ?: 0
        val size = params["size"]?.toInt() ?: 10

        call.respond(recipeSearch.search(query, from, size))
      }
    }
  }
```


---

[previous](coroutines.md) | [parent](index.md)

This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/RecipeSearchEngine.kt) from which this page is generated.