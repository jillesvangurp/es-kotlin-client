[previous](search.md) | [parent](index.md) | [next](coroutines.md)
---

# Query DSL

Elasticsearch has a Query DSL and the Java Rest High Level Client comes with a very expansive
set of builders that you can use to programmatically construct queries. Of course builders are 
something that you should avoid in Kotlin. 

On this page, we'll demonstrate how you can use this Java API effectively from Kotlin in a series of 
examples that get us increasingly closer to a more idiomatic way of using Kotlin.

Using the same example index as we used earlier:

```kotlin
val results = thingRepository.search {

  source(
    searchSource()
      .size(20)
      .query(
        boolQuery()
          .must(matchQuery("title", "quick").boost(2.0f))
          .must(matchQuery("title","brown"))
      )
  )
}
println("We found ${results.totalHits} results.")
```

Output:

```
We found 3 results.

```

This is unfortunately quite ugly from a Kotlin point of view. Lets see if we can clean that up a little.

```kotlin

// more idomatic Kotlin using apply { ... }
val results = thingRepository.search {
  source(searchSource().apply {
    query(
      boolQuery().apply {
        must().apply {
          add(matchQuery("title", "quick").boost(2.0f))
          add(matchQuery("title", "brown"))
        }
      }
    )
  })
}
println("We found ${results.totalHits} results.")
```

Output:

```
We found 3 results.

```

This is better but still a little verbose. To improve on this, a few extension functions can help.

```kotlin

// more idomatic Kotlin using apply { ... }
val results = thingRepository.search {
  // one of our extension functions gets rid of a bit of ugliness here
  source {
    query(
      boolQuery().apply {
        must().apply {
          add(matchQuery("title", "quick").boost(2.0f))
          add(matchQuery("title", "brown"))
        }
      }
    )
  }
}
println("We found ${results.totalHits} results.")
```

Output:

```
We found 3 results.

```


---

[previous](search.md) | [parent](index.md) | [next](coroutines.md)

This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/QueryDslManualTest.kt) from which this page is generated.