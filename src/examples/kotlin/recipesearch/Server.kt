package recipesearch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
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
import org.elasticsearch.client.asyncIndexRepository
import org.elasticsearch.client.create
import org.elasticsearch.cluster.health.ClusterHealthStatus

suspend fun main(vararg args: String) {
    val objectMapper = ObjectMapper()
    // enable Kotlin integration and whatever else is on the classpath
    objectMapper.findAndRegisterModules()
    // make sure we convert names with underscores properly to and from kotlin (camelCase)
    objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE

    val esClient = create(host = "localhost", port = 9999)
    // shut down client cleanly after ktor exits
    esClient.use {
        val recipeRepository = esClient.asyncIndexRepository<Recipe>(index = "recipes")
        val recipeSearch = RecipeSearch(recipeRepository, objectMapper)
        if(args.any { it == "-c" }) {
            recipeSearch.deleteIndex()
            recipeSearch.createNewIndex()
            recipeSearch.indexExamples()
        }

        val server = embeddedServer(Netty, port = 8080) {
            install(DataConversion) // this will allow us to serialize data objects to json
            install(ContentNegotiation) {
                // lets reuse our mapper for this
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }

            routing {
                get("/") {
                    call.respondText("Hello World!", ContentType.Text.Plain)
                }
                post("/recipe_index") {
                    recipeSearch.createNewIndex()
                    call.respond(HttpStatusCode.Created)
                }

                delete("/recipe_index") {
                    recipeSearch.deleteIndex()
                    call.respond(HttpStatusCode.Gone)
                }

                post("/index_examples") {
                    recipeSearch.indexExamples()
                    call.respond(HttpStatusCode.Accepted)
                }

                get("/health") {
                    val healthStatus = recipeSearch.healthStatus()
                    if (healthStatus == ClusterHealthStatus.RED) {
                        call.respond(HttpStatusCode.ServiceUnavailable, "es cluster is $healthStatus")
                    } else {
                        call.respond(HttpStatusCode.OK, "es cluster is $healthStatus")
                    }
                }

                get("/search") {
                    val query = call.request.queryParameters["q"].orEmpty()
                    val from = call.request.queryParameters["from"]?.toInt() ?: 0
                    val size = call.request.queryParameters["size"]?.toInt() ?: 10

                    call.respond(recipeSearch.search(query, from, size))
                }
            }
        }
        server.start(wait = true)
    }
}