[previous](search.md) | [parent](index.md) | [next](coroutines.md)
---

# Query DSL

Elasticsearch has a Query DSL and the Java Rest High Level Client comes with a very expansive
set of builders that you can use to programmatically construct queries. Of course builders are 
something that you should avoid in Kotlin. 

On this page we outline a few ways in which you can build queries both programmatically using the builders
that come with the Java client, using json strings, and using our Kotlin DSL.

We will use the same example as before in [Search](search.md). 

## Java Builders

The Java client comes with `org.elasticsearch.index.query.QueryBuilders` which provides static methods 
to create builders for the various queries. This covers most but probably not all of the query DSL 
but should cover most commonly used things.

```kotlin
val results = thingRepository.search {
  source(
    searchSource()
      .size(20)
      .query(
        boolQuery()
          .must(matchQuery("title", "quick").boost(2.0f))
          .must(matchQuery("title", "brown"))
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

Using `apply` gets rid of the need to chain all the calls and it is a little better but still a little verbose. 

## Kotlin Search DSL

To address this, this library provides a DSL that allows you to mix both type safe DSL constructs 
and simple schema-less manipulation of maps. We'll show several versions of the same query above to
show how this works.

The example below uses the type safe way to set up the same query as before.

```kotlin
// more idomatic Kotlin using apply { ... }
val results = thingRepository.search {
  // SearchRequest.dsl is the extension function that allows us to use the dsl.
  dsl {
    // SearchDSL is passed to the block as this
    // It extends our MapBackedProperties class
    // This allows us to delegate properties to a MutableMap

    // from is a property that is stored in the map
    from = 0

    // MapBackedProperties actually implements MutableMap
    // and delegates to a simple MutableMap.
    // so this works too: this["from"] = 0

    // Unfortunately Maps have their own size property so we can't
    // use that as a property name for the query size :-(
    resultSize = 20
    // this actually puts a key "size" in the map

    // query is a function that takes an ESQuery instance
    query =
      // bool is a function that create a BoolQuery,
      // which extends ESQuery, that is injected into the block
      bool {
        // BoolQuery has a function called must
        // it also has filter, should, and mustNot
        must(
          // it has a vararg list of ESQuery
          MatchQuery("title", "quick") {
            // match always needs a field and query
            // but boost is optional
            boost = 2.0
          },
          // but the block param is nullable and
          // defaults to null
          MatchQuery("title", "brown")
        )
      }

  }
}
println("We found ${results.totalHits} results.")
```

Output:

```
We found 3 results.

```

If you want to use it in schemaless mode or want to use things that aren't part of the DSL
this is easy too.

```kotlin
// more idomatic Kotlin using apply { ... }
val results = thingRepository.search {
  // SearchRequest.dsl is the extension function that allows us to use the dsl.
  dsl {
    this["from"] = 0
    this["size"] = 10
    query =
      // custom query constructs an object with an object inside
      // as elasticsearch expects.
      customQuery("bool") {
        // the inner object is a MapBackedProperties instance
        // which is a MutableMap<String,Any>
        // so we can assign a list to the must key
        this["must"] = listOf(
          // match is another customQuery
          customQuery("match") {
            // elasticsearch expects fieldName: object
            // so we use mapProps to construct and use
            // another MapBackedProperties
            this["title"] = mapProps {
              this["query"] = "quick"
              this["boost"] = 2.0
            }
          }.toMap(),
          customQuery("match") {
            this["title"] = mapProps {
              this["query"] = "brown"
            }
          }.toMap()
        )
      }
  }
}
println("We found ${results.totalHits} results.")
```

Output:

```
We found 3 results.

```

## Extending the DSL

The Elasticsearch DSL is huge and only a small part is covered in our Kotlin DSL so far. Using the DSL
in schema-less mode allows you to work around this and you can of course mix both approaches.

However, if you need something added to the DSL it is really easy to do this yourself. For example 
this is the implementation of the match we use above. 

```kotlin
enum class MatchOperator { AND, OR }

@Suppress("EnumEntryName")
enum class ZeroTermsQuery { all, none }

@SearchDSLMarker
class MatchQueryConfig : MapBackedProperties() {
  var query by property<String>()
  var boost by property<Double>()
  var analyzer by property<String>()
  var autoGenerateSynonymsPhraseQuery by property<Boolean>()
  var fuzziness by property<String>()
  var maxExpansions by property<Int>()
  var prefixLength by property<Int>()
  var transpositions by property<Boolean>()
  var fuzzyRewrite by property<String>()
  var lenient by property<Boolean>()
  var operator by property<MatchOperator>()
  var minimumShouldMatch by property<String>()
  var zeroTermsQuery by property<ZeroTermsQuery>()
}

@SearchDSLMarker
class MatchQuery(
  field: String,
  query: String,
  matchQueryConfig: MatchQueryConfig = MatchQueryConfig(),
  block: (MatchQueryConfig.() -> Unit)? = null
) : ESQuery(name = "match") {
  // The map is empty until we assign something
  init {
    this[field] = matchQueryConfig
    matchQueryConfig.query = query
    block?.invoke(matchQueryConfig)
  }
}
```

Writing your own EsQuery subclass should be straight-forward. Just extend `EsQuery` and write a function 
that constructs it.

The DSL is currently kind of experimental and very incomplete. I will add more to this over time.


---

[previous](search.md) | [parent](index.md) | [next](coroutines.md)

This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/QueryDslManualTest.kt) from which this page is generated.