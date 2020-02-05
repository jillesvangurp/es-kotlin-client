package io.inbot.eskotlinwrapper.manual.recipesearch

// BEGIN model classes
data class Author(val name: String, val url: String)

data class Recipe(
    val title: String,
    val description: String,
    val ingredients: List<String>,
    val directions: List<String>,
    val prep_time_min: Int,
    val cook_time_min: Int,
    val servings: Int,
    val tags: List<String>,
    val author: Author,
    val source_url: String
)
// END model classes
