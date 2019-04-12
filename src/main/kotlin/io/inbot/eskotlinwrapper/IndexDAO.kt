package io.inbot.eskotlinwrapper

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
     * create the index.
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
     * Delete the index associated with the dao.
     *
     * @return true if successful or false if the index did not exist
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
     * @return a set of the current `AliasMetaData` associated with the `indexName`.
     */
    fun currentAliases(requestOptions: RequestOptions = this.defaultRequestOptions): Set<AliasMetaData> {
        return client.indices().getAlias(GetAliasesRequest().indices(indexName), requestOptions).aliases[this.indexName]
            ?: throw IllegalStateException("Inde $indexName does not exist")
    }

    /**
     * Index a document.
     *
     * @param create set to false for upserts. Otherwise it fails on indexing documents that already exist.
     */
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
            @Suppress("DEPRECATION")
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
     * Update document by fetching the current version with `get` and then applying the `transformFunction` to produce the updated version.
     *
     * @param maxUpdateTries if > 0, it will deal with version conflicts (e.g. due to concurrent updates) by retrying with the latest version.
     */
    fun update(
        id: String,
        maxUpdateTries: Int = 2,
        requestOptions: RequestOptions = this.defaultRequestOptions,
        transformFunction: (T) -> T
    ) {
        update(0, id, transformFunction, maxUpdateTries, requestOptions)
    }

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
                @Suppress("DEPRECATION")
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
     * Delete an object.
     */
    fun delete(id: String, requestOptions: RequestOptions = this.defaultRequestOptions) {
        val deleteRequest = DeleteRequest().index(indexWriteAlias).id(id)
        if (!type.isNullOrBlank()) {
            @Suppress("DEPRECATION")
            deleteRequest.type(type)
        }

        client.delete(deleteRequest, requestOptions)
    }

    /**
     * @return deserialized object.
     */

    fun get(id: String): T? {
        return getWithGetResponse(id)?.first
    }

    /**
     * @return a `Pair` of the deserialized object and the `GetResponse` with all the relevant metadata.
     */
    fun getWithGetResponse(
        id: String,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ): Pair<T, GetResponse>? {
        val getRequest = GetRequest().index(indexReadAlias).id(id)
        if (!type.isNullOrBlank()) {
            @Suppress("DEPRECATION")
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
     * Create a `BulkIndexingSession` and use it with the `operationsBlock`.
     *
     * @see [BulkIndexingSession]
     */
    fun bulk(
        bulkSize: Int = 100,
        retryConflictingUpdates: Int = 0,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
        itemCallback: ((BulkIndexingSession.BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
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
     * Create a `BulkIndexingSession`.
     *
     * @see [BulkIndexingSession]
     */
    fun bulkIndexer(
        bulkSize: Int = 100,
        retryConflictingUpdates: Int = 0,
        refreshPolicy: WriteRequest.RefreshPolicy = WriteRequest.RefreshPolicy.WAIT_UNTIL,
        itemCallback: ((BulkIndexingSession.BulkOperation<T>, BulkItemResponse) -> Unit)? = null,
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
     * Call the refresh API on elasticsearch. You should not use this other than in tests. E.g. when testing search queries, you often want to refresh after indexing before calling search
     *
     * @throws UnsupportedOperationException if you do not explicitly opt in to this by setting the `refreshAllowed parameter on the dao`.
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
     * Perform a search against your index.
     *
     * @param block customise your search request in the block
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
     * Perform an asynchronous search against your index.
     *
     * @param block customise your search request in the block
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