package su.itgalley

import org.http4k.core.HttpHandler
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.handlers.getBlocksWithLevelsHandler
import su.itgalley.handlers.getSubtasksByLevelHandler

fun createRouter(
    blockDao: BlockDao,
    levelDao: LevelDao,
    subtaskDao: SubtaskDao,
    hotKeyDao: HotKeyDao,
): HttpHandler {
    val staticHandler = static(Classpath("frontend"))

    val app: HttpHandler =
        routes(
            "/" bind staticHandler,
            "/api/blocks" bind getBlocksWithLevelsHandler(blockDao, levelDao),
            "/levels/{levelId}/subtasks" bind getSubtasksByLevelHandler(levelDao, subtaskDao, hotKeyDao),
        )

    return app
}
