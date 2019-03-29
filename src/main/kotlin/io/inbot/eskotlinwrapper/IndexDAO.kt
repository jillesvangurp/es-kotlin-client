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
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.Request
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.doSearch
import org.elasticsearch.client.doSearchAsync
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.cluster.metadata.AliasMetaData
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.common.xcontent.XContentType

private val logger = KotlinLogging.logger {}

/**
 * DAO (Data Access Object) abstraction that allows you to work with indices.
 *
 * You should create a DAO for each index you work with. You need to specify a [ModelReaderAndWriter] for serialization and deserialization.
 *
 * @see [RestHighLevelClient.crudDao] for a convenient way to create a dao.
 *
 * @param T the type of the object that is stored in the index.
 * @param indexName name of the index
 * @param indexReadAlias Alias used for read operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.
 * @param indexWriteAlias Alias used for write operations. If you are using aliases, you can separate reads and writes. Defaults to indexName.
 * @param type the type of the documents in the index; defaults to "doc". Since ES 6, there can only be one type. Types will be deprecated in ES 7 and removed in ES 8.
 * @param modelReaderAndWriter serialization of your model class.
 * @param refreshAllowed if false, the [refresh] will throw an exception. Defaults to false.
 * @param defaultRequestOptions passed on all API calls. Defaults to [RequestOptions.DEFAULT]. Use this to set custom headers or override on each call on the dao.
 *
 */
class IndexDAO<T : Any>(
    val indexName: String,
    private val client: RestHighLevelClient,
    private val modelReaderAndWriter: ModelReaderAndWriter<T>,
    private val refreshAllowed: Boolean = false,
    val type: String = "doc", // default to using "doc", note types will soon be removed but seem required for now
    val indexWriteAlias: String = indexName,
    val indexReadAlias: String = indexWriteAlias,
    private val defaultRequestOptions: RequestOptions = RequestOptions.DEFAULT

) {
    fun createIndex(
        requestOptions: RequestOptions = this.defaultRequestOptions,
        block: CreateIndexRequest.() -> Unit
    ) {

        val indexRequest = CreateIndexRequest(indexName)
        block.invoke(indexRequest)

        client.indices().create(indexRequest, requestOptions)
    }

    fun deleteIndex(requestOptions: RequestOptions = this.defaultRequestOptions) {
        client.indices().delete(DeleteIndexRequest(indexName), requestOptions)
    }

    fun currentAliases(requestOptions: RequestOptions = this.defaultRequestOptions): Set<AliasMetaData> {
        return client.indices().getAlias(GetAliasesRequest().indices(indexName), requestOptions).aliases[this.indexName] ?: throw IllegalStateException("Inde $indexName does not exist")
    }

    fun index(
        id: String,
        obj: T,
        create: Boolean = true,
        version: Long? = null,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ) {
        val indexRequest = IndexRequest()
            .index(indexWriteAlias)
            .type(type)
            .id(id)
            .create(create)
            .source(modelReaderAndWriter.serialize(obj), XContentType.JSON)
        if (version != null) {
            indexRequest.version(version)
        }
        client.index(
            indexRequest, requestOptions
        )
    }

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
            val response =
                client.get(GetRequest().index(indexWriteAlias).type(type).id(id), requestOptions)
            val currentVersion = response.version

            val sourceAsBytes = response.sourceAsBytes
            if (sourceAsBytes != null) {
                val currentValue = modelReaderAndWriter.deserialize(sourceAsBytes)
                val transformed = transformFunction.invoke(currentValue)
                index(id, transformed, create = false, version = currentVersion)
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

    fun delete(id: String, requestOptions: RequestOptions = this.defaultRequestOptions) {
        client.delete(DeleteRequest().index(indexWriteAlias).type(type).id(id), requestOptions)
    }

    fun get(id: String): T? {
        return getWithGetResponse(id)?.first
    }

    fun getWithGetResponse(
        id: String,
        requestOptions: RequestOptions = this.defaultRequestOptions
    ): Pair<T, GetResponse>? {
        val response = client.get(GetRequest().index(indexReadAlias).type(type).id(id), requestOptions)
        val sourceAsBytes = response.sourceAsBytes

        if (sourceAsBytes != null) {
            val deserialized = modelReaderAndWriter.deserialize(sourceAsBytes)

            return Pair(deserialized, response)
        }
        return null
    }

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

    fun refresh() {
        if (refreshAllowed) {
            // calling this is not safe in production settings but highly useful in tests
            client.lowLevelClient.performRequest(Request("POST", "/$indexWriteAlias/_refresh"))
        } else {
            throw UnsupportedOperationException("refresh is not allowed; you need to opt in by setting refreshAllowed to true")
        }
    }

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

        val searchResponse = client.doSearch(requestOptions, wrappedBlock)
        return if (searchResponse.scrollId == null) {
            PagedSearchResults(searchResponse, modelReaderAndWriter)
        } else {
            ScrollingSearchResults(
                searchResponse,
                modelReaderAndWriter,
                client,
                scrollTtlInMinutes
            )
        }
    }

    suspend fun searchAsync(
        requestOptions: RequestOptions = this.defaultRequestOptions,
        block: SearchRequest.() -> Unit
    ): SearchResults<T> {
        // FIXME figure out how to return a scrolling of this with scrolling search and a suspending sequence
        val wrappedBlock: SearchRequest.() -> Unit = {
            this.indices(indexReadAlias)

            block.invoke(this)
        }

        val searchResponse = client.doSearchAsync (requestOptions, wrappedBlock)
        return PagedSearchResults(searchResponse, modelReaderAndWriter)
    }
}