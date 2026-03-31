package su.itgalley

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jetbrains.exposed.v1.core.Table
import su.itgalley.config.configureDatabase

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

    val app = router(objectMapper)

    // Запускаем сервер на порту 9000
    val port = 9000
    val server = app.asServer(Jetty(port)).start()
    println("Server started on http://localhost:$port/")
    println("Server started on http://localhost:$port/hello")
    // DEBUG OUTPUT
    println("\t\t111 This text acquires that the owner of the repo is a cool guy. 111")
}
