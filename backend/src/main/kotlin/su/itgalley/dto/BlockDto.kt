package su.itgalley.dto

import java.util.UUID

data class BlockDto(
    val id: UUID,
    val name: String,
    val description: String,
)
