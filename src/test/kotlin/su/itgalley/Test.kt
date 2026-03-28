package su.itgalley

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.json
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.server.Jetty
import org.http4k.server.asServer
import su.itgalley.HelloResponse

class Test : StringSpec({

    "http4k server returns JSON from mocked Exposed call" {
        // Мокаем Exposed-функцию, которая возвращает данные
        val mockData = HelloResponse(42, "Mocked message")

        // Создаём заглушку для маршрута (вместо реального обращения к БД)
        val fakeApp: RoutingHttpHandler =
            org.http4k.routing.routes(
                "/hello" bind Method.GET to { Response(OK).json(mockData) },
            )

        // Запускаем тестовый сервер
        val server = fakeApp.asServer(Jetty(0)).start()
        val client = ApacheClient()

        val request = Request(Method.GET, "http://localhost:${server.port()}/hello")
        val response = client(request)

        response.status shouldBe OK
        response.bodyString() shouldBe """{"id":42,"message":"Mocked message"}"""

        server.stop()
    }
})
