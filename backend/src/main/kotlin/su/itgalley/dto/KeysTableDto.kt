package su.itgalley.dto

import su.itgalley.database.schema.KeyGroup
import java.util.UUID

data class KeysTableDto(
    val id: UUID,
    val key: String,
    val keyGroup: KeyGroup,
)
