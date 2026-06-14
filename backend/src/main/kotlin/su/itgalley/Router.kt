package su.itgalley

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.LevelHelpDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.dao.TutorialDao
import su.itgalley.handlers.getLevelSubtasksHandler
import su.itgalley.handlers.getNavigationHandler

fun createRouter(
    blockDao: BlockDao,
    levelDao: LevelDao,
    subtaskDao: SubtaskDao,
    hotKeyDao: HotKeyDao,
    tutorialDao: TutorialDao,
    levelHelpDao: LevelHelpDao,
): HttpHandler {
    val apiRoutes =
        routes(
            "/" bind { _: Request -> Response(Status.OK).body("api is running :P") },
            "/api/navigation" bind getNavigationHandler(blockDao, levelDao),
            "/api/levels/{levelId}" bind getLevelSubtasksHandler(levelDao, subtaskDao, hotKeyDao, tutorialDao, levelHelpDao),
        )

    // CORS-обёртка
    return object : HttpHandler {
        override fun invoke(request: Request): Response {
            // Обработка предварительного запроса OPTIONS
            if (request.method == Method.OPTIONS) {
                return Response(Status.OK)
                    .header("Access-Control-Allow-Origin", "http://localhost:3000")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept")
            }
            // Обычный запрос – добавляем заголовок к ответу
            val response = apiRoutes(request)
            return response.header("Access-Control-Allow-Origin", "http://localhost:3000")
        }
    }
}
