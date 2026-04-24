package su.itgalley

import org.http4k.core.HttpHandler
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static

fun createRouter(): HttpHandler {
    val staticHandler = static(Classpath("frontend"))

    val app: HttpHandler =
        routes(
            "/" bind staticHandler,
        )

    return app
}
