package io.inbot.search.escrud

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.client.RestHighLevelClient

inline fun <reified T:Any> RestHighLevelClient.crudDao(index:String, objectMapper: ObjectMapper=ObjectMapper().findAndRegisterModules(), refreshAllowed:Boolean=false): ElasticSearchCrudDAO<T> {
    return ElasticSearchCrudDAO<T>(index,T::class,this, objectMapper,refreshAllowed=refreshAllowed)
}
