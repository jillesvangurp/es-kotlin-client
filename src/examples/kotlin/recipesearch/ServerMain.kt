package recipesearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import io.inbot.eskotlinwrapper.JacksonModelReaderAndWriter
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.DataConversion
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import org.elasticsearch.client.asyncIndexRepository
import org.elasticsearch.client.create
import org.elasticsearch.cluster.health.ClusterHealthStatus

// BEGIN main_function
@ExperimentalCoroutinesApi
suspend fun main(vararg args: String) {
    val objectMapper = ObjectMapper()
    // enable Kotlin integration and whatever else is on the classpath
    objectMapper.findAndRegisterModules()
    // make sure we convert names with underscores properly to and
    // from kotlin (camelCase)
    objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

    val esClient = create(host = "localhost", port = 9999)
    // shut down client cleanly after ktor exits
    esClient.use {
        val customSerde = JacksonModelReaderAndWriter(Recipe::class, objectMapper)
        val recipeRepository =
            esClient.asyncIndexRepository<Recipe>(
                index = "recipes",
                // we override the default because we want to reuse the objectMapper
                // and reuse our snake case setup
                modelReaderAndWriter = customSerde
            )
        val recipeSearch = RecipeSearch(recipeRepository, objectMapper)
        if (true || args.any { it == "-c" }) {
            // if you pass -c it bootstraps an index
            recipeSearch.deleteIndex()
            recipeSearch.createNewIndex()
            recipeSearch.indexExamples()
        }

        // creates a simple ktor server
        createServer(objectMapper, recipeSearch).start(wait = true)
    }
}
// END main_function

@ExperimentalCoroutinesApi
// BEGIN ktor_setup
private fun createServer(
    objectMapper: ObjectMapper,
    recipeSearch: RecipeSearch
): NettyApplicationEngine {
    return embeddedServer(Netty, port = 8080) {
        // this will allow us to serialize data objects to json
        install(DataConversion)
        install(ContentNegotiation) {
            // lets reuse our mapper for this
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }

        routing {
            get("/") {
                call.respondText("Hello World!", ContentType.Text.Plain)
            }
            post("/recipe_index") {
                withContext(Dispatchers.Default) {
                    recipeSearch.createNewIndex()
                    call.respond(HttpStatusCode.Created)
                }
            }

            delete("/recipe_index") {
                withContext(Dispatchers.Default) {
                    recipeSearch.deleteIndex()
                    call.respond(HttpStatusCode.Gone)
                }
            }

            post("/index_examples") {
                withContext(Dispatchers.Default) {
                    recipeSearch.indexExamples()
                    call.respond(HttpStatusCode.Accepted)
                }
            }

            get("/health") {
                withContext(Dispatchers.Default) {

                    val healthStatus = recipeSearch.healthStatus()
                    if (healthStatus == ClusterHealthStatus.RED) {
                        call.respond(
                            HttpStatusCode.ServiceUnavailable,
                            "es cluster is $healthStatus"
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            "es cluster is $healthStatus"
                        )
                    }
                }
            }

            get("/search") {
                withContext(Dispatchers.Default) {

                    val params = call.request.queryParameters
                    val query = params["q"].orEmpty()
                    val from = params["from"]?.toInt() ?: 0
                    val size = params["size"]?.toInt() ?: 10

                    call.respond(recipeSearch.search(query, from, size))
                }
            }

            get("/autocomplete") {
                withContext(Dispatchers.Default) {
                    val params = call.request.queryParameters
                    val query = params["q"].orEmpty()
                    val from = params["from"]?.toInt() ?: 0
                    val size = params["size"]?.toInt() ?: 10

                    call.respond(recipeSearch.autocomplete(query, from, size))
                }
            }
        }
    }
    // END ktor_setup

}