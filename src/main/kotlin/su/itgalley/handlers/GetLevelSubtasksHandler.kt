package su.itgalley.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.json
import org.http4k.routing.path
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.schema.SolutionType
import su.itgalley.dto.SubtaskResponseDto
import java.util.UUID

fun getLevelSubtasksHandler(
    levelDao: LevelDao,
    subtaskDao: SubtaskDao,
    hotKeyDao: HotKeyDao,
): HttpHandler =
    { request ->
        val response =
            run {
                val levelIdStr =
                    request.path("levelId")
                        ?: return@run Response(Status.BAD_REQUEST).body("Missing level ID")

                val levelId =
                    try {
                        UUID.fromString(levelIdStr)
                    } catch (_: IllegalArgumentException) {
                        return@run Response(Status.BAD_REQUEST).body("Invalid level ID format")
                    }

                // Проверяем существование уровня
                val level = levelDao.findById(levelId) ?: return@run Response(Status.NOT_FOUND).body("Level not found")

                // Получаем подзадачи уровня с их позициями (уже отсортированные)
                val subtasksWithPos = subtaskDao.getSubtasksByLevelWithPosition(levelId)

                val responseSubtasks =
                    subtasksWithPos.map { (subtask, _) ->
                        val combination =
                            when (subtask.solutionType) {
                                SolutionType.HOTKEY -> {
                                    // Получаем ключи комбинации через HotKeyDao
                                    val hotKey = subtask.keySolutionId?.let { hotKeyDao.findById(it) }
                                    if (hotKey != null) {
                                        hotKeyDao
                                            .getKeysForCombination(hotKey.keyCombinationId)
                                            .sortedBy { it.position }
                                            .map { mapOf("key" to it.key) }
                                    } else {
                                        emptyList()
                                    }
                                }

                                SolutionType.TYPING -> {
                                    emptyList()
                                }
                            }
                        SubtaskResponseDto(
                            id = subtask.id,
                            solutionType = subtask.solutionType,
                            description = subtask.description,
                            combination = combination,
                        )
                    }

                Response(Status.OK).json(responseSubtasks.asJsonObject())
            }

        response
    }
