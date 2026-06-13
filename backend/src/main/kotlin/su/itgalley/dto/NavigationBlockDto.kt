package su.itgalley.dto

import java.util.UUID

data class NavigationBlockDto(
    val id: UUID,
    val name: String,
    val description: String?,
    val levels: List<NavigationLevelDto>,
)
