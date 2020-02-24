[previous](creating-client.md) | [parent](index.md) | [next](bulk-indexing.md)
---

# Working with objects

To do anything with Elasticsearch we have to store documents in some index. The Java client
provides everything you need to do this but using it the right way requires quite a bit of boiler plate 
as well as a deep understanding of what needs doing.

An important part of this library is providing a user friendly abstraction for this that 
should be familiar if you've ever written a database application using modern frameworks such
as Spring, Ruby on Rails, etc. In such frameworks a  Repository 
provides primitives for interacting with objects in a particular database table.

Wee provide a similar abstraction the [`IndexRepository`](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/main/kotlin/io/inbot/eskotlinwrapper/IndexRepository.kt). You create one for each 
index that you have and it allows you to do Create, Read, Update, and Delete (CRUD) operations as well as 
a few other things.

Since Elasticsearch stores Json documents, we'll want to use a data class to represent on the 
Kotlin side and let the `IndexRepository` take care of serializing/deserializing.

## Creating an `IndexRepository`

Lets use a simple data class with a few fields.

```kotlin
data class Thing(val title: String, val amount: Long = 42)
```

Now we can create an `IndexRepository` for our `Thing` using the `indexRepository` extension function:

```kotlin
// we pass in the index name
val thingRepository = esClient.indexRepository<Thing>("things")
```

## Creating the index

Before we store any objects, we should create the index. Note this is optional but using
Elasticsearch in schema-less mode is probably not what you want. We use a simple mapping here.

```kotlin
thingRepository.createIndex {
  // use our friendly DSL to configure the index
  configure {
    settings {
      replicas = 0
      shards = 1
    }
    mappings {
      // in the block you receive FieldMappings as this
      // a simple text field "title": {"type":"text"}
      text("title")
      // a numeric field with sub fields, use generics
      // to indicate what kind of number
      number<Long>("amount") {
        // we can customize the FieldMapping object
        // that we receive in the block
        fields {
          // we get another FieldMappings
          // lets add a keyword field
          keyword("somesubfield")
          // if you want, you can manipulate the
          // FieldMapping as a map
          // this is great for accessing features
          // not covered by our Kotlin DSL
          this["imadouble"] = mapOf("type" to "double")
          number<Double>("abetterway")
        }
      }
    }
  }
}
```

Note. The mapping DSL is a work in progress. The goal is not to support everything as you
can simply fall back to Kotlin's Map DSL with `mapOf("myfield" to someValue)`. Both `FieldMapping`
and `FieldMappings` extend a `MapBackedProperties` class that delegates to a `MutableMap`. This allows 
us to have type safe properties and helper methods and mix that with raw map access where our DSL misses
features.

```kotlin
// stringify is a useful extension function we added to the response
println(thingRepository.getSettings().stringify(true))

thingRepository.getMappings().mappings()
  .forEach { (name, meta) ->
  print("$name -> ${meta.source().string()}")
}
```

Output:

```
{
  "things" : {
  "settings" : {
    "index" : {
    "creation_date" : "1582585487130",
    "number_of_shards" : "1",
    "number_of_replicas" : "0",
    "uuid" : "8iBk_xFSQN-z9U92i1N_Aw",
    "version" : {
      "created" : "7060099"
    },
    "provided_name" : "things"
    }
  }
  }
}
things -> {"_meta":{"content_hash":"VFD04UkOGUHI+2GGDIJ8PQ==","timestamp":"2020-
02-24T23:04:47.102989Z"},"properties":{"amount":{"type":"long","fields":{"abette
rway":{"type":"double"},"imadouble":{"type":"double"},"somesubfield":{"type":"ke
yword"}}},"title":{"type":"text"}}}
```

Of course you can also simply set the settings json using source. This is 
useful if you maintain your mappings as separate json files.

```kotlin
// delete the previous version of our index
thingRepository.deleteIndex()
// create a new one using json source
thingRepository.createIndex {
  source("""
    {
      "settings": {
      "index": {
        "number_of_shards": 3,
        "number_of_replicas": 0,
        "blocks": {
        "read_only_allow_delete": "false"
        }
      }
      },
      "mappings": {
      "properties": {
        "title": {
        "type": "text"
        },
        "amount": {
        "type": "long"
        }
      }
      }
    }
  """)
}
```

## CRUD operations

Now that we have an index, we can use the CRUD operations.

```kotlin
println("Object does not exist: ${thingRepository.get("first")}")
// so lets store something
thingRepository.index("first", Thing("A thing", 42))

println("Now we get back our object: ${thingRepository.get("first")}")
```

Output:

```
Object does not exist: null
Now we get back our object: Thing(title=A thing, amount=42)

```

You can't index an object twice unless you opt in to it being overwritten.

```kotlin
try {
  thingRepository.index("first", Thing("A thing", 42))
} catch (e: ElasticsearchStatusException) {
  println("we already had one of those and es returned ${e.status().status}")
}
// this how you do upserts
thingRepository.index("first", Thing("Another thing", 666), create = false)
println("It was changed: ${thingRepository.get("1")}")
```

Output:

```
we already had one of those and es returned 409
It was changed: null

```

Of course deleting an object is also possible.

```kotlin
thingRepository.delete("1")
println(thingRepository.get("1"))
```

Output:

```
null

```

## Updates with optimistic locking

One useful feature in Elasticsearch is that it can do optimistic locking. The way this works is
that it keeps track of a `sequenceNumber` and `primaryTerm` for each document. 
If you provide both in index, it will check that what you provide matches what it has and only
overwrite the document if that lines up.

This works as follows.

```kotlin
thingRepository.index("2", Thing("Another thing"))

val (obj, rawGetResponse) = thingRepository.getWithGetResponse("2")
  ?: throw IllegalStateException("We just created this?!")

println("obj with id '${obj.title}' has id: ${rawGetResponse.id}, " +
    "primaryTerm: ${rawGetResponse.primaryTerm}, and " +
    "seqNo: ${rawGetResponse.seqNo}")
// This works
thingRepository.index(
  "2",
  Thing("Another Thing"),
  seqNo = rawGetResponse.seqNo,
  primaryTerm = rawGetResponse.primaryTerm,
  create = false
)
try {
  // ... but if we use these values again it fails
  thingRepository.index(
    "2",
    Thing("Another Thing"),
    seqNo = rawGetResponse.seqNo,
    primaryTerm = rawGetResponse.primaryTerm,
    create = false
  )
} catch (e: ElasticsearchStatusException) {
  println("Version conflict! Es returned ${e.status().status}")
}
```

Output:

```
obj with id 'Another thing' has id: 2, primaryTerm: 1, and seqNo: 3
Version conflict! Es returned 409

```

While you can do this manually, the Kotlin client makes optimistic locking a bit easier by 
providing a robust update method instead.

```kotlin
thingRepository.index("3", Thing("Yet another thing"))

thingRepository.update("3") { currentThing ->
  currentThing.copy(title = "an updated thing", amount = 666)
}

println("It was updated: ${thingRepository.get("3")}")

thingRepository.update("3") { currentThing ->
  currentThing.copy(title = "we can do this again and again", amount = 666)
}

println("It was updated again ${thingRepository.get("3")}")
```

Output:

```
It was updated: Thing(title=an updated thing, amount=666)
It was updated again Thing(title=we can do this again and again, amount=666)

```

Update simply does the same as we did manually earlier: it gets the current version of the object
along with the metadata. It then passes the current version to the update lambda function where
you can do with it what you want. In this case we simply use Kotlin's copy to create a copy and 
modify one of the fields and then we return it as the new value. 

The `update` method  traps the version conflict and retries a configurable number of times. 
Conflicts can happen if you have concurrent writes to the same object. The retry gets the latest 
version and applies the update lambda again and then attempts to store that.

To simulate what happens without retries, we can throw some threads at this and configure 0
retries:

```kotlin
thingRepository.index("4", Thing("First version of the thing", amount = 0))

try {
  1.rangeTo(30).toList().parallelStream().forEach { n ->
    // the maxUpdateTries parameter is optional and has a default value of 2
    // so setting this to 0 and doing concurrent updates is going to fail
    thingRepository.update("4", 0) { Thing("nr_$n") }
  }
} catch (e: Exception) {
  println("It failed because we disabled retries and we got a conflict")
}
```

Output:

```
It failed because we disabled retries and we got a conflict

```

Doing the same with 10 retries, fixes the problem.

```kotlin
thingRepository.index("5", Thing("First version of the thing", amount = 0))

1.rangeTo(30).toList().parallelStream().forEach { n ->
  // but if we let it retry a few times, it will be eventually consistent
  thingRepository.update("5", 10) { Thing("nr_$n", amount = it.amount + 1) }
}
println("All updates succeeded! amount = ${thingRepository.get("5")?.amount}.")
```

Output:

```
All updates succeeded! amount = 30.

```

## Custom serialization 

If you want to customize how serialization and deserialization works, you can pass in a
[`ModelReaderAndWriter`](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/main/kotlin/io/inbot/eskotlinwrapper/ModelReaderAndWriter.kt) implementation.

The default value of this is an instance of the included JacksonModelReaderAndWriter, which
uses Jackson to serialize and deserialize our `Thing` objects. 

If you don't want the default Jackson based serialization, or if you want to customize the jackson 
object mapper, you simply create your own instance and pass it to the `IndexRepository`.

```kotlin
// this is what is used by default but you can use your own implementation
val modelReaderAndWriter = JacksonModelReaderAndWriter(
  Thing::class,
  ObjectMapper().findAndRegisterModules()
)

val thingRepository = esClient.indexRepository<Thing>(
  index = "things", modelReaderAndWriter = modelReaderAndWriter)
```

## Co-routine support

As with most of this library, the same functionality is also available in a co-routine friendly
variant `AsyncIndexRepository`. To use that, you need to use `esClient.asyncIndexRepository`. 

This works almost the same as the synchronous version except all of the functions are marked as 
suspend on the `AsyncIndexRepository` class. To call these, you will need to call these from a
`CoRoutineScope`.

For more details on how to use co-routines, see [Co-routines](coroutines.md)


---

[previous](creating-client.md) | [parent](index.md) | [next](bulk-indexing.md)

This Markdown is Generated from Kotlin code. Please don't edit this file and instead edit the [source file](https://github.com/jillesvangurp/es-kotlin-wrapper-client/tree/master/src/test/kotlin/io/inbot/eskotlinwrapper/manual/CrudManualTest.kt) from which this page is generated.