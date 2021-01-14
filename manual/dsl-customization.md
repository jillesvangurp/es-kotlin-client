[previous](coroutines.md) | [index](index.md) | [next](recipe-search-engine.md)

___

# Extending and Customizing DSLs 

The provided Kotlin DSLs for mappings, settings, or querying are nice but don't cover 100% of what Elasticsearch provides. And Elasticsearch keeps on adding new things to their client library with each new release so it is quite hard for us to keep up with that. Consequently we made a choice to focus on supporting the commonly used things; or at least things we ourselves use. 

Luckily, it is quite easy to work around this and either extend our DSLs to add support for new things, or simply fall back to using the underlying DSL for constructing arbitrary JSON structures that our DSLs are built on.

## How does this work?

Elasticsearch provides a REST api that accepts JSON. So the goal of our DSLs is to programmatically construct an object tree that can be serialized to a JSON structure that matches what Elasticsearch expects and that is ultimately sent over the network via the Elasticsearch `LowLevelClient`.

For serialization, we piggy back on `XContent` which is the built in framework that the elasticsearch client uses for dealing with JSON content. 

To do this, we make use of a few useful Kotlin language features. One of these is interface delegation, which we use to implement a special base class: `MapBackedProperties`. That implements a simple `Map<String, Any>` using interface delegation. 

In most places in the DSL where you are provided a block, the receiver is an object of this type; or a derived class with that type as its parent. This enables us to define properties that are under the hood simply inserted into the map delegate. 

Simply extending this class makes it possible to define class properties that delegate to this map. Additionally, enables users of your custom class to simply put values to the map directly for anything that you forgot to add as a delegated property.

## Example: the TermQuery Implementation

As an example, here is the implementation of the TermQuery in our library:

```kotlin
class TermQueryConfig : MapBackedProperties() {
  var value by property<String>()
  var boost by property<Double>()
}

@SearchDSLMarker
class TermQuery(
  field: String,
  value: String,
  termQueryConfig: TermQueryConfig = TermQueryConfig(),
  block: (TermQueryConfig.() -> Unit)? = null
) : ESQuery("term") {

  init {
    putNoSnakeCase(field, termQueryConfig)
    termQueryConfig.value = value
    block?.invoke(termQueryConfig)
  }
}
```

`TermQuery` extends a base class called `ESQuery`, which in turn is a MapBackedProperties with a single field (the query name) mapped to another `MapBackedProperties` (the query details). From there on it is pretty straightforward: TermQuery has two constructor parameters: `field` and `value`. field is used as the key to yet another `MapBackedProperties` object with the `TermConfiguration` which in this case contains things like the value and the boost.

```kotlin
val termQuery = TermQuery("myField", "someValue") {
  boost = 10.0
}

println(termQuery.toString())
```

Captured Output:

```
{
  "term" : {
  "myField" : {
    "value" : "someValue",
    "boost" : 10.0
  }
  }
}

```

As you can see, termQuery inherits a convenient `toString` implementation that prints JSON. This is useful for debugging and logging if you ar programmatically creating queries using the DSL.

Also note how we use delegated properties in the `TermConfiguration`. This allows us to set values to these properties when using the DSL using a simple assignment.

Suppose we forgot to add something here and you need to set a (non existing) property named foo on a the term query configuration:

```kotlin
val termQuery = TermQuery("myField", "someValue") {
  // we support boost
  boost=2.0
  // but foo is not something we support
  // but we can still add it
  this["foo"] = "bar"
}

println(termQuery)
```

Captured Output:

```
{
  "term" : {
  "myField" : {
    "value" : "someValue",
    "boost" : 2.0,
    "foo" : "bar"
  }
  }
}

```

Obviously, Elasticsearch would reject this query with a bad request because there is no `foo` property.

## Creating more complex JSON

You can construct arbitrary json pretty easily. If you want to create a blank object, use `mapProps`

```kotlin
val aCustomObject = mapProps {
  this["icanhasjson"] = listOf(1,2,"4")
  this["meaning_of_life"] = 42
  this["nested_object"] = mapProps {
    this["some more stuff"] = "you get the point"
  }
}

println(aCustomObject)
```

Captured Output:

```
{
  "icanhasjson" : [
  1,
  2,
  "4"
  ],
  "meaning_of_life" : 42,
  "nested_object" : {
  "some more stuff" : "you get the point"
  }
}

```

You can put lots of different types in the map. To enable XContent to serialize things, we use the `writeAny` extension function. It currently supports most primitives, maps, enums, iterables, and more.

## XContent extensions

`XContent` is something that Elasticsearch, and the Elasticsearch Java client uses internally for dealing with JSON content. As this may be quite alien to people used to dealing with e.g. Jackson, GSon, or kotlinx-serialization, this library provides some extension functions to make dealing with XContent straightforward. Mostly the problem boils down to somehow providing XContent to java library functions that expect some kind of json structure as a parameter. 

Most of these Java functions come with a builder that will typically accept either a Java builder that produces the right XContent, or just any XContent object; typically via a builder function called `source`.

As mentioned in the DSL function, you can of course use these builders. But the builder pattern is of course not that nice from Kotlin, which is the reason we provide a Kotlin DSL as well.

But sometimes you just want to bypass the builders and provide some json straight to Elasticsearch. For this, we provide a few `source` extension functions on `SearchRequest`, `CountRequest`, and a few other requests that take either a string (or Kotlin multi line string) or a reader with raw json. 

The `MapBackedProperties` mentioned above of course implements the `ToXContent` interface, which allows us to use any instance of that to be passed to the before mentioned source functions. 

## Extending the DSL

We've covered most of the basic queries in the search DSL but currently we are adding to this only on a need to have basis. However, should you have a need for something we do not yet provide, it is very easy to extend the DSL.
Simply extend ESQuery and use delegated properties as explained above. Of course pull requests with new query types or improvements to the existing ones are welcome.

There are also functions in the Elasticsearch client that have their own DSL that we have not covered yet. For these, you can of course also create your own DSLs. And of course pull requests for this are very much appreciated as well.


___

[previous](coroutines.md) | [index](index.md) | [next](recipe-search-engine.md)

