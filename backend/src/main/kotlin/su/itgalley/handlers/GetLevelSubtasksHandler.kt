package su.itgalley.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.json
import org.http4k.routing.path
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.LevelHelpDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.dao.TutorialDao
import su.itgalley.database.schema.SolutionType
import su.itgalley.dto.SubtaskResponseDto
import java.util.UUID

@Suppress("LongMethod")
fun getLevelSubtasksHandler(
    levelDao: LevelDao,
    subtaskDao: SubtaskDao,
    hotKeyDao: HotKeyDao,
    tutorialDao: TutorialDao,
    levelHelpDao: LevelHelpDao,
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

                val level =
                    levelDao.findById(levelId)
                        ?: return@run Response(Status.NOT_FOUND).body("Level not found")

                val subtasksWithPos = subtaskDao.getSubtasksByLevelWithPosition(levelId)

                val responseSubtasks =
                    subtasksWithPos.map { (subtask, _) ->
                        val combination =
                            when (subtask.solutionType) {
                                SolutionType.HOTKEY -> {
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
                            stringSolution =
                                if (subtask.solutionType == SolutionType.TYPING) {
                                    subtask.stringSolution
                                } else {
                                    null
                                },
                        )
                    }

                val tutorialContent = level.tutorialId?.let { tutorialDao.findById(it)?.content }
                val helpContent = level.levelHelpId?.let { levelHelpDao.findById(it)?.content }

                val result =
                    mapOf(
                        "tutorial" to tutorialContent,
                        "help" to helpContent,
                        "subtasks" to responseSubtasks,
                    )

                Response(Status.OK).json(result.asJsonObject())
            }
        response
    }
