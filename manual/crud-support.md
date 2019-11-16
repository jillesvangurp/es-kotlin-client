[previous](creating-client.md) | [parent](index.md) | [next](bulk-indexing.md)
---

# Working with objects

To do anything with Elasticsearch we have to store documents in some index. The Java client
provides everything you need to do this but using it the right way requires a deep understanding of
what needs to be done.

An important part of Kotlin Wrapper is providing a user friendly abstraction for this that 
should be familiar if you've ever written a database application using modern frameworks such
as Spring, Ruby on Rails, etc. In such frameworks a Data Access Object or Repository object 
provides primitives for interacting with objects in a particular database table.

With the Kotlin client, we provide a similar abstraction the `IndexDAO`. You create one for each 
index that you have and it allows you to do Create, Read, Update, and Delete operations as well as 
a few other things.

Since Elasticsearch stores Json documents, we'll want to use a data class to represent on the 
Kotlin side and let the client take care of serializing/deserializing.

## Creating an IndexDAO

Lets use a simple data class with a few fields.

```kotlin
data class Thing(val name: String, val amount: Long = 42)
```

Now we can create an IndexDAO using the `crudDao` extension function:

```kotlin
// lets use jackson to serialize our Thing
// other serializers may be supported by implementing ModelReaderAndWriter
val modelReaderAndWriter = JacksonModelReaderAndWriter(
    Thing::class,
    ObjectMapper().findAndRegisterModules()
)

// Create a Data Access Object
val thingDao = esClient.crudDao("things", modelReaderAndWriter)
```

## Preparing the index

Before we store any objects, we should create the index. Note this is optional but using
Elasticsearch in schema-less mode is probably not what you want.

```kotlin
// Probably a good idea to get those from some file.
val settings = """
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
          "name": {
            "type": "text"
          },
          "amount": {
            "type": "long"
          }
        }
      }
    }
"""
thingDao.createIndex {
    source(settings, XContentType.JSON)
}
```

That creates the index with a simple mapping.

```kotlin
// prints null
println(thingDao.get("first"))
// so lets store something
thingDao.index("first", Thing("A thing", 42))
// Now we can get it and it's a data class so it has a `toString()`
println(thingDao.get("first"))
```

Output:

```
null
Thing(name=A thing, amount=42)

```

You can't index an object twice unless you opt in to it being overridden.

```kotlin
try {
    thingDao.index("first", Thing("A thing", 42))
} catch (e: ElasticsearchStatusException) {
    println("we already had one of those and es returned ${e.status().status}")
}
// this how you do upserts
thingDao.index("1", Thing("Another thing", 666), create = false)
println(thingDao.get("1"))
```

Output:

```
we already had one of those and es returned 409
Thing(name=Another thing, amount=666)

```

And of course deleting an object is also possible.

```kotlin
thingDao.delete("1")
println(thingDao.get("1"))
```

Output:

```
null

```

## Updates with optimistic locking

One useful feature in Elasticsearch is that it can do optimistic locking. The way this works is
that it keeps track of a `sequenceNumber` and `primaryTerm`. If you provide both in index, it will 
check that what you provide matches what it has and only overwrite the document if that lines up.

This works as follows.

```kotlin
thingDao.index("2", Thing("Another thing"))

// we know it exists, so !!
val (obj, rawGetResponse) = thingDao.getWithGetResponse("2")!!
println("obj '${obj.name}' has id: ${rawGetResponse.id}, primaryTerm: ${rawGetResponse.primaryTerm}, and seqNo: ${rawGetResponse.seqNo}")
// works
thingDao.index(
    "2",
    Thing("Another Thing"),
    seqNo = rawGetResponse.seqNo,
    primaryTerm = rawGetResponse.primaryTerm,
    create = false
)
try {
    // but fails the second time
    thingDao.index(
        "2",
        Thing("Another Thing"),
        seqNo = rawGetResponse.seqNo,
        primaryTerm = rawGetResponse.primaryTerm,
        create = false
    )
} catch (e: ElasticsearchStatusException) {
    println("Version conflict because sequence number changed! Es returned ${e.status().status}")
}
```

Output:

```
obj 'Another thing' has id: 2, primaryTerm: 1, and seqNo: 0
Version conflict because sequence number changed! Es returned 409

```

While you can do this manually, the Kotlin client makes this a bit easier by providing a robust 
update method instead and do away with all the boiler plate above.

```kotlin
thingDao.index("3", Thing("Another thing"))

println(thingDao.get("3"))

thingDao.update("3") { currentThing ->
    currentThing.copy(name = "an updated thing", amount = 666)
}

println(thingDao.get("3"))

thingDao.update("3") { currentThing ->
    currentThing.copy(name = "we can do this again and again", amount = 666)
}

println(thingDao.get("3"))
```

Output:

```
Thing(name=Another thing, amount=42)
Thing(name=an updated thing, amount=666)
Thing(name=we can do this again and again, amount=666)

```

The update simply does the same as we did manually earlier: it gets the current version, 
it then passes the current version to the update lambda function where you can do with it what you 
want. In this case we simply use Kotlin's copy to create a copy and modify one of the fields and 
then we return it as the new value. 

The `update` also traps the version conflict and retries a configurable number of times. This can happen
if you have concurrent writes to the same object. The retry gets the latest version and applies
the update lambda again and then attempts to store that.

To simulate what happens, we can throw some threads at this.

```kotlin
thingDao.index("4", Thing("First version of the thing", amount = 0))

try {
    1.rangeTo(30).toList().parallelStream().forEach { n ->
        // the maxUpdateTries parameter is optional and has a default value of 2
        // so setting this to 0 and doing concurrent updates is going to fail
        thingDao.update("4", 0) { Thing("nr_$n") }
    }
} catch (e: Exception) {
    println("Oops it failed because we disabled retries")
}
thingDao.index("5", Thing("First version of the thing", amount = 0))

1.rangeTo(30).toList().parallelStream().forEach { n ->
    // but if we let it retry a few times, it will be eventually consistent
    thingDao.update("5", 10) { Thing("nr_$n", amount = it.amount + 1) }
}
println("All the updates succeeded: amount = ${thingDao.get("5")?.amount}.")
```

Output:

```
Oops it failed because we disabled retries
All the updates succeeded: amount = 30.

```


---

[previous](creating-client.md) | [parent](index.md) | [next](bulk-indexing.md)