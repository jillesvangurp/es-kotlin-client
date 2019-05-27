package org.elasticsearch.search

import io.inbot.eskotlinwrapper.ModelReaderAndWriter

/**
 * Shortcut to hits.map.
 */
fun <T : Any> SearchHits.mapHits(fn: (SearchHit) -> T): List<T> {
    return this.hits.map(fn)
}

/**
 * Deserialize the results as a sequence.
 */
fun <T : Any> SearchHits.mapHits(modelReaderAndWriter: ModelReaderAndWriter<T>): Sequence<T> {
    return this.hits.asSequence()
        .map { modelReaderAndWriter.deserialize(it) }
}
