package su.itgalley.dto

import java.util.UUID

data class HotKeyDto(
    val id: UUID,
    val blockId: Int,
    val description: String,
    val keyCombinationId: UUID,
)
