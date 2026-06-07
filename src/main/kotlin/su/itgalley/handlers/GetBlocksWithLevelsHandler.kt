package su.itgalley.handlers

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Jackson.asJsonObject
import org.http4k.format.Jackson.json
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.dto.BlockWithLevelsDto

fun getBlocksWithLevelsHandler(
    blockDao: BlockDao,
    levelDao: LevelDao,
): HttpHandler =
    { _ ->
        val blocks = blockDao.findAll()
        val result =
            blocks.map { block ->
                val levels = levelDao.findByBlockOrdered(block.id)
                BlockWithLevelsDto(
                    id = block.id,
                    name = block.name,
                    description = block.description,
                    levels = levels,
                )
            }
        Response(Status.OK).json(result.asJsonObject())
    }

// [
// {
//    "id": "550e8400-e29b-41d4-a716-446655440000",
//    "name": "Название блока",
//    "description": "Описание блока (может быть null или пустая строка)",
//    "levels": [
//    {
//        "id": "11111111-1111-1111-1111-111111111111",
//        "name": "Уровень 1",
//        "blockId": "550e8400-e29b-41d4-a716-446655440000",
//        "position": 1,
//        "tutorialId": null,
//        "taskId": "22222222-2222-2222-2222-222222222222",
//        "levelHelpId": null,
//        "requiredInBlock": "No"
//    },
//    {
//        "id": "33333333-3333-3333-3333-333333333333",
//        "name": "Уровень 2",
//        "blockId": "550e8400-e29b-41d4-a716-446655440000",
//        "position": 2,
//        "tutorialId": null,
//        "taskId": "44444444-4444-4444-4444-444444444444",
//        "levelHelpId": null,
//        "requiredInBlock": "Yes"
//    }
//    ]
// }
// ]
