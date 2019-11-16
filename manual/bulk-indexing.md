[previous](crud-support.md) | [parent](index.md) | [next](search.md)
---

# Bulk Indexing

An important part of working with Elasticsearch is adding content. While the CRUD support is useful
for manipulating individual objects in an index, it is not suitable for sending large amounts of data.

For that, bulk indexing should be used. The bulk API in Elasticsearch is one of the more complex APIs
The Kotlin client provides a few key abstractions to make using this API easy and straightforward.

## Using the DAO to bulk index

Again we use our `Thing` class and `thingDao`

```kotlin
data class Thing(val name: String, val amount: Long = 42)
```

The API for bulk indexing encapsulates all of the complexity for dealing with bulk. In the simplest form
we simply do the following.

```kotlin
// creates a BulkIndexingSession<Thing> and passes it to the block
thingDao.bulk {
    1.rangeTo(5).forEach {
        index("doc-$it", Thing("indexed $it", 666))
    }
}

//
println("Indexed: " + 1.rangeTo(5).joinToString(", ") {
    thingDao.get("doc-$it")!!.name
})
```

Output:

```
Indexed: indexed 1, indexed 2, indexed 3, indexed 4, indexed 5

```

There are a few alternative things you can do.

```kotlin
thingDao.bulk {
    // setting create=false overwrites and is the appropriate thing
    // to do if you are replacing documents in bulk
    index("doc-1", Thing("upserted 1", 666), create = false)

    // you can do a safe bulk update similar to the normal update like this
    // this has the disadvantage of doing 1 get per item and may not scale
    getAndUpdate("doc-2") { currentVersion ->
        // this works just like the update on the dao and it will retry a configurable number
        // of times.
        currentVersion.copy(name = "updated 2")
    }

    // if you already have the seqNo, primary term, and current version
    // there you can skip the get. A good way to get these would be
    // a scrolling search.
    update(
        id = "doc-3",
        // yes, these two values are wrong; but it falls back to doing a getAndUpdate.
        seqNo = 12,
        primaryTerms = 34,
        original = Thing("indexed $it", 666)
    ) { currentVersion ->
        currentVersion.copy(name = "safely updated 3")
    }
    // and of course you can delete items
    delete("doc-4")
}

println(thingDao.get("doc-1"))
println(thingDao.get("doc-2"))
println(thingDao.get("doc-3"))
// should print null
println(thingDao.get("doc-4"))
```

Output:

```
Thing(name=upserted 1, amount=666)
Thing(name=updated 2, amount=666)
Thing(name=indexed 3, amount=666)
null

```

## Fine-tuning how bulk works

The bulk method has a few extra parameters with defaults that you 
can override to fine tune how bulk works

```kotlin
thingDao.bulk(
    // this controls the number of items to send to Elasticsearch
    bulkSize = 10,
    // this controls how often documents are retried
    retryConflictingUpdates = 3,
    // this controls how Elasticsearch refreshes and whether
    // the bulk request blocks until ES has refreshed or not
    refreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
    // retries are implemented using a default implementation of itemCallback
    // however, you can override this
    itemCallback = object : (BulkOperation<Thing>, BulkItemResponse) -> Unit {
        // Elasticsearch confirms what it did for each item in a bulk request
        // and you can implement this callback to do something custom
        override fun invoke(op: BulkOperation<Thing>, response: BulkItemResponse) {
            if (response.isFailed) {
                println("${op.id}: ${op.operation.opType().name} failed: ${response.failureMessage}")
            } else {
                println("${op.id}: ${op.operation.opType().name} succeeded")
            }
        }
    }
) {

    delete("doc-1")
    update(
        id = "doc-2",
        // these values are wrong and this will now fail instead of retry
        seqNo = 12,
        primaryTerms = 34,
        original = Thing("updated 2", 666)
    ) { currentVersion ->
        currentVersion.copy(name = "safely updated 3")
    }
}
```

Output:

```
doc-1: DELETE succeeded
doc-2: UPDATE failed: [things/XCllD4bgRNSZto79UD6Txw][[things][2]] ElasticsearchException[Elasticsearch exception [type=version_conflict_engine_exception, reason=[doc-2]: version conflict, required seqNo [12], primary term [34]. current document has seqNo [1] and primary term [1]]]

```



                    ---

[previous](crud-support.md) | [parent](index.md) | [next](search.md)