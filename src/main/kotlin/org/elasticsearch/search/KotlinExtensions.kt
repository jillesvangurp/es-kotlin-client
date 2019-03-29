package org.elasticsearch.search

import io.inbot.eskotlinwrapper.ModelReaderAndWriter

fun <T : Any> SearchHits.mapHits(fn: (SearchHit) -> T): List<T> {
    return this.hits.map(fn)
}

fun <T : Any> SearchHits.mapHits(modelReaderAndWriter: ModelReaderAndWriter<T>): Sequence<T> {
    return this.hits.asSequence()
        .map { it -> modelReaderAndWriter.deserialize(it) }
}
