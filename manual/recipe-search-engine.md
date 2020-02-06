[previous](coroutines.md) | [parent](index.md)
---

# Building a Recipe Search Engine

The Elastic Search Kotlin Wrapper is designed to simplify writing production code that
interacts with Elasticsearch.

The easiest way to demonstrate how it works is just showing it with a simple but realistic 
example. The code below is very loosely based on an example in the 
[Elastic examples repository](https://github.com/elastic/examples/tree/master/Search/recipe_search_java)

I've borrowed the data from there. To make things interesting, we'll pretend we need co-routines 
because we want to integrate this into a proper asynchronous application server like for example 
ktor.

## Our data model

Consider this simple example json file for chicken enchilladas:

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

## Creating an index



---

[previous](coroutines.md) | [parent](index.md)

This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/BuildingARealSearchEngine.kt) from which this page is generated.