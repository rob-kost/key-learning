package su.itgalley

import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val app = createRouter()
    val port = 9000
    val server = app.asServer(Jetty(port)).start()
    println("Server started on http://localhost:$port/")
}
