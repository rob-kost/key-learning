package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.Levels
import su.itgalley.database.schema.Subtasks
import su.itgalley.database.schema.TaskSubtasks
import su.itgalley.database.schema.Tasks
import su.itgalley.dto.SubTaskDto
import java.util.UUID

class SubtaskDao : BaseDao<SubTaskDto, UUID> {
    override fun findById(id: UUID): SubTaskDto? =
        transaction {
            Subtasks
                .selectAll()
                .where { Subtasks.id eq id }
                .map { rowToSubtask(it) }
                .singleOrNull()
        }

    override fun findAll(): List<SubTaskDto> =
        transaction {
            Subtasks.selectAll().map { rowToSubtask(it) }
        }

    override fun save(entity: SubTaskDto): SubTaskDto =
        transaction {
            if (findById(entity.id) == null) {
                Subtasks.insert {
                    it[Subtasks.id] = entity.id
                    it[Subtasks.description] = entity.description
                    it[Subtasks.solutionType] = entity.solutionType
                    it[Subtasks.stringSolution] = entity.stringSolution
                    it[Subtasks.keySolutionId] = entity.keySolutionId
                }
            } else {
                Subtasks.update({ Subtasks.id eq entity.id }) {
                    it[Subtasks.description] = entity.description
                    it[Subtasks.solutionType] = entity.solutionType
                    it[Subtasks.stringSolution] = entity.stringSolution
                    it[Subtasks.keySolutionId] = entity.keySolutionId
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean = transaction { Subtasks.deleteWhere { Subtasks.id eq id } > 0 }

    // получить список всех подзадач по блоку (id)
    fun getSubtasksByBlock(blockId: UUID): List<SubTaskDto> =
        transaction {
            (
                Subtasks
                    .innerJoin(TaskSubtasks)
                    .innerJoin(Tasks)
                    .innerJoin(Levels)
            ).selectAll()
                .where { Levels.blockId eq blockId }
                .withDistinct() // чтобы избежать дубликатов подзадач
                .map { rowToSubtask(it) }
        }

    // получить список подзадач по задаче (id)
    fun getSubtasksByTask(levelId: UUID): List<SubTaskDto> =
        transaction {
            (Tasks innerJoin TaskSubtasks innerJoin Subtasks)
                .selectAll()
                .where { Tasks.id eq levelId }
                .orderBy(TaskSubtasks.position to SortOrder.ASC)
                .map { rowToSubtask(it) }
        }

    // получить список подзадач по уровню (id)
    fun getSubtasksByLevel(levelId: UUID): List<SubTaskDto> =
        transaction {
            (Levels innerJoin Tasks innerJoin TaskSubtasks innerJoin Subtasks)
                .selectAll()
                .where { Levels.id eq levelId }
                .orderBy(TaskSubtasks.position to SortOrder.ASC)
                .map { rowToSubtask(it) }
        }

    fun rowToSubtask(row: ResultRow): SubTaskDto =
        SubTaskDto(
            id = row[Subtasks.id],
            description = row[Subtasks.description],
            solutionType = row[Subtasks.solutionType],
            stringSolution = row[Subtasks.stringSolution],
            keySolutionId = row[Subtasks.keySolutionId],
        )

    // Метод для получения всех подзадач для уровня с сортировкой
    fun getSubtasksByLevelWithPosition(levelId: UUID): List<Pair<SubTaskDto, Int>> =
        transaction {
            (Levels innerJoin Tasks innerJoin TaskSubtasks innerJoin Subtasks)
                .selectAll()
                .where { Levels.id eq levelId }
                .orderBy(TaskSubtasks.position to SortOrder.ASC, Subtasks.id to SortOrder.ASC)
                .map { row ->
                    Pair(
                        rowToSubtask(row),
                        row[TaskSubtasks.position],
                    )
                }
        }
}
