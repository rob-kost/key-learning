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
import java.util.UUID

fun getSubtasksByLevelHandler(
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

                // 1. Получаем описание уровня (из задачи)
                val description =
                    levelDao.getLevelTaskDescription(levelId)
                        ?: return@run Response(Status.NOT_FOUND).body("Level not found")

                // 2. Получаем все подзадачи уровня с их позициями (отсортированные)
                val subtasksWithPos = subtaskDao.getSubtasksByLevelWithPosition(levelId)
                if (subtasksWithPos.isEmpty()) {
                    // Нет подзадач – возвращаем уровень с пустой комбинацией
                    val levelObject =
                        mapOf(
                            "id" to levelId.toString(),
                            "description" to description,
                            "combination" to emptyList<Any>(),
                        )
                    return@run Response(Status.OK).json(listOf(levelObject).asJsonObject())
                }

                // 3. Собираем все клавиши из горячих клавиш подзадач
                val allKeys = mutableListOf<Map<String, Any>>()
                var globalNumber = 1

                for ((subtask, _) in subtasksWithPos) {
                    if (subtask.solutionType.name == "hotkey" && subtask.keySolutionId != null) {
                        // Получаем HotKeyDto по keySolutionId
                        val hotKey = hotKeyDao.findById(subtask.keySolutionId)
                        if (hotKey != null) {
                            val keys = hotKeyDao.getKeysForCombination(hotKey.keyCombinationId)
                            for (kp in keys) {
                                allKeys.add(
                                    mapOf(
                                        "key" to kp.key,
                                        "number" to globalNumber++,
                                    ),
                                )
                            }
                        }
                    }
                }

                // 4. Формируем объект уровня (один)
                val levelObject =
                    mapOf(
                        "id" to levelId.toString(),
                        "description" to description,
                        "combination" to allKeys,
                    )

                Response(Status.OK).json(listOf(levelObject).asJsonObject())
            }
        response
    }

// [
// {
//    "id": "550e8400-e29b-41d4-a716-446655440000",
//    "description": "Удерживайте комбинацию: Ctrl + Shift + S + C + F5(пробел) + K",
//    "combination": [
//    { "key": "Control", "number": 1 },
//    { "key": "Shift",   "number": 2 },
//    { "key": "S",       "number": 3 },
//    { "key": "C",       "number": 4 },
//    { "key": "F5",      "number": 5 },
//    { "key": "K",       "number": 6 }
//    ]
// }
// ]
