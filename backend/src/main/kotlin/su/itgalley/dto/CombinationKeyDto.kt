package su.itgalley.dto

import java.util.UUID

data class CombinationKeyDto(
    val id: UUID,
    val combinationId: UUID,
    val keyId: UUID,
    val position: Int,
)
