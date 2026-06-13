package su.itgalley.dto

import su.itgalley.database.schema.SolutionType
import java.util.UUID

data class SubtaskResponseDto(
    val id: UUID,
    val solutionType: SolutionType,
    val description: String,
    val combination: List<Map<String, String>>,
    val expectedText: String?,
)
