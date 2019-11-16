[previous](bulk-indexing.md) | [parent](index.md)
---

# Search

```kotlin
// lets use a slightly different model class this time
data class Thing(val title: String)
```

And lets use these settings and mappings:

```

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
          }
        }
      }
    }

```

```kotlin
// for testing, it's useful to allow calling refresh. Note we disallow this by default because this
// whould not be used in production.
val thingDao = esClient.crudDao("things", modelReaderAndWriter, refreshAllowed = true)
```

Lets index some documents to look for ...

```kotlin
thingDao.bulk {
    index("1", Thing("The quick brown fox"))
    index("2", Thing("The quick brown emu"))
    index("3", Thing("The quick brown gnu"))
    index("4", Thing("Another thing"))
    5.rangeTo(100).forEach {
        index("$it", Thing("Another thing: $it"))
    }
}
// force ES to commit everything to disk so search works right away
thingDao.refresh()
```

## Doing a simple JSON search.


```kotlin
// a SearchRequest is created and passed into the block
val results = thingDao.search {
    // we can use templating
    val text = "brown"
    source("""
        {
            "query": {
                "match": {
                    "title": {
                        "query": "$text"
                    }
                }
            }
        }
    """.trimIndent())
}
println("Found ${results.totalHits}")

// get the deserialized thing from the search response
results.mappedHits.forEach {
    println(it)
}

// we can also get the underlying SearchHit
results.searchHits.first().apply {
    println("Hit: ${id}\n${sourceAsString}")
}

// or we can get both as Pair
results.hits.first().apply {
    val (searchHit,deserialized) = this
    println("Hit: ${searchHit.id} deserialized from\n ${searchHit.sourceAsString}\nto\n$deserialized")
}
```

Output:

```
Found 3
Thing(title=The quick brown fox)
Thing(title=The quick brown emu)
Thing(title=The quick brown gnu)
Hit: 1
{"title":"The quick brown fox"}
Hit: 1 deserialized from
 {"title":"The quick brown fox"}
to
Thing(title=The quick brown fox)

```

## Scrolling searches

Elasticsearch has a notion of scrolling searches for retrieving large amounts of 
documents from an index. Normally this works by keeping track of a scroll token and
passing that to Elasticsearch to fetch subsequent pages of results.

To make this easier and less tedious, the search method on the dao has a simpler solution.

```kotlin
// simply set scrolling to true
val results = thingDao.search(scrolling = true) {
    source("""
        {
            "size": 2,
            "query": {
                "match_all": {}
            }
        }
    """.trimIndent())
}

// with size: 2, this will page through ten pages of results before stopping
results.mappedHits.take(20).forEach {
    println(it)
}
// after the block exits, the scroll is cleaned up with an extra request
```

Output:

```
Thing(title=Another thing: 5)
Thing(title=Another thing: 7)
Thing(title=Another thing: 13)
Thing(title=Another thing: 22)
Thing(title=Another thing: 24)
Thing(title=Another thing: 26)
Thing(title=Another thing: 41)
Thing(title=Another thing: 42)
Thing(title=Another thing: 43)
Thing(title=Another thing: 44)
Thing(title=Another thing: 51)
Thing(title=Another thing: 52)
Thing(title=Another thing: 53)
Thing(title=Another thing: 54)
Thing(title=Another thing: 56)
Thing(title=Another thing: 57)
Thing(title=Another thing: 59)
Thing(title=Another thing: 61)
Thing(title=Another thing: 62)
Thing(title=Another thing: 65)

```



                    ---

[previous](bulk-indexing.md) | [parent](index.md)