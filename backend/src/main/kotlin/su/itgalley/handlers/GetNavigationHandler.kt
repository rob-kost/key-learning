package su.itgalley.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.json
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.dto.NavigationBlockDto
import su.itgalley.dto.NavigationLevelDto

fun getNavigationHandler(
    blockDao: BlockDao,
    levelDao: LevelDao,
): HttpHandler =
    { _ ->
        val blocks = blockDao.findAllSorted()
        val result =
            blocks.map { block ->
                val levels = levelDao.findByBlockOrdered(block.id)
                NavigationBlockDto(
                    id = block.id,
                    name = block.name,
                    description = block.description,
                    levels = levels.map { NavigationLevelDto(it.id, it.name) },
                )
            }
        Response(Status.OK).json(result.asJsonObject())
    }
