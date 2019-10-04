package io.inbot.eskotlinwrapper

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import mu.KotlinLogging
import org.apache.commons.lang3.RandomUtils
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.bulk.BulkItemResponse
import org.elasticsearch.action.delete.DeleteRequest
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.support.ActiveShardCount
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.Request
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.search
import org.elasticsearch.client.searchAsync
import org.elasticsearch.cluster.metadata.AliasMetaData
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType
import java.lang.IllegalArgumentException

private val logger = KotlinLogging.logger {}

/**
 * DAO (Data Access Object) abstraction that allows you to work with indices.
 *
 * You should create a DAO for each index you work with. You need to specify a [ModelReaderAndWriter] for serialization and deserialization.
 *
 * @see `RestHighLevelClient.crudDao` for a convenient way to create a dao.
 *
 * @param T the type of the object that is stored in the index.
 * @param indexName name of the index
 * @param indexReadAlias Alias used for read operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.
 * @param indexWriteAlias Alias used for write operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.
 * @param type the type of the documents in the index; defaults to null. Since ES 6, there can only be one type. Types are deprecated in ES 7 and removed in ES 8.
 * @param modelReaderAndWriter serialization of your model class.
 * @param refreshAllowed if false, the refresh will throw an exception. Defaults to false.
 * @param defaultRequestOptions passed on all API calls. Defaults to `RequestOptions.DEFAULT`. Use this to set custom headers or override on each call on the dao.
 *
 */
class IndexDAO<T : Any>(
    val indexName: String,
    private val client: RestHighLevelClient,
    internal val modelReaderAndWriter: ModelReaderAndWriter<T>,
    private val refreshAllowed: Boolean = false,
    @Deprecated("Types are deprecated in ES 7.x and will be removed in v8") val type: String? = null,
    val indexWriteAlias: String = indexName,
    val indexReadAlias: String = indexWriteAlias,
    internal val defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT

) {
    /**
     * Create the index.
     *
     * @param block customize the `CreateIndexRequest`
     *
     * ```
     * {
     *   source(settings, XContentType.JSON)
     * }
     * ```
     */
    fun createIndex(
        requestOptions: RequestOptions = this.defaultRequestOptions,
        waitForActiveShards: ActiveShardCount? = null,
        block: CreateIndexRequest.() -> Unit
    ) {

        val indexRequest = CreateIndexRequest(indexName)
        if (waitForActiveShards != null) {
            indexRequest.waitForActiveShards(waitForActiveShards)
        }
        block.invoke(indexRequest)

        client.indices().create(indexRequest, requestOptions)
    }

    /**
     * Delete the index associated with the dao. Returns true if successful or false if the index did not exist
     */
    fun deleteIndex(requestOptions: RequestOptions = this.defaultRequestOptions): Boolean {
        try {
            client.indices().delete(DeleteIndexRequest(indexName), requestOptions)
            return true
        } catch (e: ElasticsearchStatusException) {
            if (e.status().status == 404) {
                // 404 means there was nothing to delete
                return false
            } else {
                // this would be unexpected
                throw e
            }
        }
    }

    /**
     * Returns a set of the current `AliasMetaData` associated with the `indexName`.
     */
    fun currentAliases(requestOptions: RequestOptions = this.defaultRequestOptions): Set<AliasMetaData> {
        return client.indices().getAlias(GetAliasesRequest().indices(indexName), requestOptions).aliases[this.indexName]
            ?: throw IllegalStateException("Inde $indexName does not exist")
    }

    /**
     * Index a document with a given `id`. Set `create` to `false` for upserts. Otherwise it fails on creating documents that already exist.
     *
     * You can optionally specify `seqNo` and `primaryTerm` to implement optimistic locking. However, you should use
     * [update] which does this for you.
     */
    @Suppress("DEPRECATION")
    fun index(
        id: String,
        obj: T,
        create: Boolean = true,
        seqNo: Long? = null,
        primaryTerm: Long? = null,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ) {
        val indexRequest = IndexRequest()
            .index(indexWriteAlias)
            .id(id)
            .create(create)
            .source(modelReaderAndWriter.serialize(obj), XContentType.JSON)
        if (!type.isNullOrBlank()) {
            indexRequest.type(type)
        }
        if (seqNo != null) {
            indexRequest.setIfSeqNo(seqNo)
            indexRequest.setIfPrimaryTerm(primaryTerm ?: throw IllegalArgumentException("you must also set primaryTerm when setting a seqNo"))
        }
        client.index(
            indexRequest, requestOptions
        )
    }

    /**
     * Updates document identified by `id` by fetching the current version with [get] and then applying the [transformFunction] to produce the updated version.
     *
     * if [maxUpdateTries] > 0, it will deal with version conflicts (e.g. due to concurrent updates) by retrying with the latest version.
     */
    fun update(
        id: String,
        maxUpdateTries: Int = 2,
        requestOptions: RequestOptions = this.defaultRequestOptions,
        transformFunction: (T) -> T
    ) {
        update(0, id, transformFunction, maxUpdateTries, requestOptions)
    }

    @Suppress("DEPRECATION")
    private fun update(
        tries: Int,
        id: String,
        transformFunction: (T) -> T,
        maxUpdateTries: Int,
        requestOptions: RequestOptions

    ) {
        try {
            val getRequest = GetRequest().index(indexWriteAlias).id(id)
            if (!type.isNullOrBlank()) {
                getRequest.type(type)
            }

            val response =
                client.get(getRequest, requestOptions)

            val sourceAsBytes = response.sourceAsBytes
            if (sourceAsBytes != null) {
                val currentValue = modelReaderAndWriter.deserialize(sourceAsBytes)
                val transformed = transformFunction.invoke(currentValue)
                index(id, transformed, create = false, seqNo = response.seqNo, primaryTerm = response.primaryTerm)
                if (tries > 0) {
                    // if you start seeing this a lot, you have a lot of concurrent updates to the same thing; not good
                    logger.warn { "retry update $id succeeded after tries=$tries" }
                }
            } else {
                throw IllegalStateException("id $id not found")
            }
        } catch (e: ElasticsearchStatusException) {

            if (e.status().status == 409) {
                if (tries < maxUpdateTries) {
                    // we got a version conflict, retry after sleeping a bit (without this failures are more likely
                    Thread.sleep(RandomUtils.nextLong(50, 500))
                    update(tries + 1, id, transformFunction, maxUpdateTries, requestOptions)
                } else {
                    throw IllegalStateException("update of $id failed after $tries attempts")
                }
            } else {
                // something else is wrong
                throw e
            }
        }
    }

    /**
     * Deletes the object object identified by `id`.
     */
    @Suppress("DEPRECATION")
    fun delete(id: String, requestOptions: RequestOptions = this.defaultRequestOptions) {
        val deleteRequest = DeleteRequest().index(indexWriteAlias).id(id)
        if (!type.isNullOrBlank()) {
            deleteRequest.type(type)
        }

        client.delete(deleteRequest, requestOptions)
    }

    /**
     * Returns the deserialized [T] for the document identified by `id`.
     */

    fun get(id: String): T? {
        return getWithGetResponse(id)?.first
    }

    /**
     * Returns a `Pair` of the deserialized [T] and the `GetResponse` with all the relevant metadata.
     */
    @Suppress("DEPRECATION")
    fun getWithGetResponse(
        id: String,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ): Pair<T, GetResponse>? {
        val getRequest = GetRequest().index(indexReadAlias).id(id)
        if (!type.isNullOrBlank()) {
            getRequest.type(type)
        }

        val response = client.get(getRequest, requestOptions)
        val sourceAsBytes = response.sourceAsBytes

        if (sourceAsBytes != null) {
            val deserialized = modelReaderAndWriter.deserialize(sourceAsBytes)

            return Pair(deserialized, response)
        }
        return null
    }

    /**
     * Create a [BulkIndexingSession] and use it with the [operationsBlock]. Inside the block you can call operations
     * like `index` and other functions exposed by [BulkIndexingSession]. The resulting bulk
     * operations are automatically grouped in bulk requests of size [bulkSize] and sent off to Elasticsearch. For each
     * operation there will be a call to the [itemCallback]. This allows you to keep track of failures, do logging,
     * or implement retries. If you leave this `null`, the default callback implementation defined in
     * [BulkIndexingSession] is used.
     **
     * See [BulkIndexingSession] for the meaning of the other parameters.
     *
     */
    fun bulk(
        bulkSize: Int = 100,
        retryConflictingUpdates: Int = 0,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
        itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
        operationsBlock: BulkIndexingSession<T>.(session: BulkIndexingSession<T>) -> Unit
    ) {
        val indexer = bulkIndexer(
            bulkSize = bulkSize,
            retryConflictingUpdates = retryConflictingUpdates,
            refreshPolicy = refreshPolicy,
            itemCallback = itemCallback
        )
        // autocloseable so we flush all the items ...
        indexer.use {
            operationsBlock.invoke(indexer, indexer)
        }
    }

    /**
     * Returns a [BulkIndexingSession] for this dao. See [BulkIndexingSession] for the meaning of the other parameters.
     */
    fun bulkIndexer(
        bulkSize: Int = 100,
        retryConflictingUpdates: Int = 0,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
        itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ) = BulkIndexingSession(
        client,
        this,
        modelReaderAndWriter,
        bulkSize,
        retryConflictingUpdates = retryConflictingUpdates,
        refreshPolicy = refreshPolicy,
        itemCallback = itemCallback,
        defaultRequestOptions = requestOptions
    )

    /**
     * Asynchronous version of bulk indexing that uses Co-routines, Flow, and Channel internally. Some of this is
     * still labeled as experimental in Kotlin and may change. Using this will require setting the `kotlin.Experimental`
     * flag in your build and also at runtime.
     *
     * This works very similar to [bulk] however it uses an [AsyncBulkIndexingSession] instead of the
     * [BulkIndexingSession]. You can control which bulkDispatcher is used for sending asynchronous bulk requests. The
     * default for this is `Dispatchers.IO`. Currently, this does not run requests in parallel. However, we are
     * exploring options for this as this could potentially speed up indexing.
     *
     *
     */
    @ExperimentalCoroutinesApi
    suspend fun bulkAsync(
        bulkSize: Int = 100,
        retryConflictingUpdates: Int = 0,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
        itemCallback: ((BulkOperation<T>, BulkItemResponse) -> Unit)? = null,

        bulkDispatcher: CoroutineDispatcher? = null,
        operationsBlock: AsyncBulkIndexingSession<T>.() -> Unit
    ) {
        AsyncBulkIndexingSession.asyncBulk(
            client = client,
            dao = this,
            modelReaderAndWriter = modelReaderAndWriter,
            bulkSize = bulkSize,
            retryConflictingUpdates = retryConflictingUpdates,
            refreshPolicy = refreshPolicy,
            itemCallback = itemCallback,
            block = {
                operationsBlock.invoke(this)
            },
            bulkDispatcher = bulkDispatcher
        )
    }

    /**
     * Call the refresh API on elasticsearch. You should not use this other than in tests. E.g. when testing search
     * queries, you often want to refresh after indexing before calling search
     *
     * Throws UnsupportedOperationException if you do not explicitly opt in to this by setting the `refreshAllowed` to
     * true when creating the dao.
     */
    fun refresh() {
        if (refreshAllowed) {
            // calling this is not safe in production settings but highly useful in tests
            client.lowLevelClient.performRequest(Request("POST", "/$indexWriteAlias/_refresh"))
        } else {
            throw UnsupportedOperationException("refresh is not allowed; you need to opt in by setting refreshAllowed to true")
        }
    }

    /**
     * Perform a search against your index. This creates a `SearchRequest` that is passed into the [block] so you can
     * customize it. Inside the [block] you can manipulate the request to set a `source` and other parameters.
     *
     * Returns a [SearchResults] instance that you can use to get the deserialized results or the raw response.
     *
     * If you want to perform a scrolling search, all you have to do is set [scrolling] to true (default is false).
     * You can also set a [scrollTtlInMinutes] if you want something else than the default of 1 minute.
     */
    fun search(
        scrolling: Boolean = false,
        scrollTtlInMinutes: Long = 1,
        requestOptions: RequestOptions = this.defaultRequestOptions,
        block: SearchRequest.() -> Unit
    ): SearchResults<T> {
        val wrappedBlock: SearchRequest.() -> Unit = {
            this.indices(indexReadAlias)
            if (scrolling) {
                scroll(TimeValue.timeValueMinutes(scrollTtlInMinutes))
            }

            block.invoke(this)
        }

        val searchResponse = client.search(requestOptions, wrappedBlock)
        return if (searchResponse.scrollId == null) {
            PagedSearchResults(searchResponse, modelReaderAndWriter)
        } else {
            ScrollingSearchResults(
                searchResponse,
                modelReaderAndWriter,
                client,
                scrollTtlInMinutes,
                requestOptions
            )
        }
    }

    /**
     * Perform an asynchronous search against your index. Works similar to [search] but does not support scrolling
     * searches currently.
     */
    suspend fun searchAsync(
        requestOptions: RequestOptions = this.defaultRequestOptions,
        block: SearchRequest.() -> Unit
    ): SearchResults<T> {
        // FIXME figure out how to return a scrolling of this with scrolling search and a suspending sequence

        val searchResponse = client.searchAsync(requestOptions) {
            indices(indexReadAlias)

            block.invoke(this)
        }
        return PagedSearchResults(searchResponse, modelReaderAndWriter)
    }
}