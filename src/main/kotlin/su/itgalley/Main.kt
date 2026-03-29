package su.itgalley

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

// 1. Определяем таблицу Exposed
object HelloTable : Table("hello") {
    val id = integer("id")
    const val LEN = 255
    val message = varchar("message", LEN)
}

// 2. Data class для JSON
data class HelloResponse(val id: Int, val message: String)

// 3. Кастомный Jackson (можно использовать http4k-format-jackson)
private val objectMapper =
    jacksonObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

fun main() {
    configureDatabase()

    val app =
        routes(
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
        )

    // Запускаем сервер на порту 8080
    val port = 9000
    val server = app.asServer(Jetty(port)).start()
    println("Server started on http://localhost:$port/hello")
    // DEBUG OUTPUT
    println("\t\t111 This text acquires that the owner of the repo is a cool guy. 111")
    println("Press ENTER to stop")
    Thread.sleep(Long.MAX_VALUE)
    server.stop()
}
