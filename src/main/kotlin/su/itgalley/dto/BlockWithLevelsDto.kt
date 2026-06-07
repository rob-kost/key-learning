package su.itgalley.dto

import java.util.UUID

data class BlockWithLevelsDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val levels: List<LevelDto>,
)
