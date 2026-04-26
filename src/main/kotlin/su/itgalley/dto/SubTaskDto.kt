package su.itgalley.dto

import su.itgalley.database.schema.SolutionType
import java.util.UUID

data class SubTaskDto(
    val id: UUID,
    val description: String,
    val solutionType: SolutionType,
    val stringSolution: String,
    val keySolutionId: UUID,
)
