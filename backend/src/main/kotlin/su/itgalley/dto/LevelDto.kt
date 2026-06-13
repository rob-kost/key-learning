package su.itgalley.dto

import su.itgalley.database.schema.RequiredInBlock
import java.util.UUID

data class LevelDto(
    val id: UUID,
    val name: String,
    val blockId: UUID,
    val position: Int,
    val tutorialId: UUID?,
    val taskId: UUID,
    val levelHelpId: UUID?,
    val requiredInBlock: RequiredInBlock,
)
