package su.itgalley.dto

import java.util.UUID

data class TaskSubtaskDto(
    val id: UUID,
    val taskId: UUID,
    val subtaskId: UUID,
    val position: Int,
)
