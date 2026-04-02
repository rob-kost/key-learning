package su.itgalley

import com.fasterxml.jackson.databind.ObjectMapper
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun router(objectMapper: ObjectMapper): HttpHandler {
    val staticHandler = static(Classpath("public"))

    val app: HttpHandler =
        routes(
            "/" bind GET to {
                Response(FOUND).header("Location", "/index.html")
            },
            "/hello" bind GET to {
                val result =
                    transaction {
                        HelloTable.selectAll().map {
                            HelloResponse(
                                id = it[HelloTable.id],
                                message = it[HelloTable.message],
                            )
                        }.firstOrNull()
                    }
                if (result != null) {
                    // Сериализуем в JSON через Jackson
                    val json = objectMapper.writeValueAsString(result)
                    Response(OK).body(json).header("Content-Type", "application/json")
                } else {
                    Response(NOT_FOUND).body("No hello found")
                }
            },
            "/" bind staticHandler,
        )

    return app
}
