package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.Subtasks
import su.itgalley.database.schema.TaskSubtasks
import su.itgalley.dto.SubTaskDto
import su.itgalley.dto.TaskSubtaskDto
import java.util.UUID

class TaskSubtaskDao : BaseDao<TaskSubtaskDto, UUID> {
    override fun findById(id: UUID): TaskSubtaskDto? =
        transaction {
            TaskSubtasks.selectAll().where { TaskSubtasks.id eq id }
                .map { rowToTaskSubtask(it) }
                .singleOrNull()
        }

    override fun findAll(): List<TaskSubtaskDto> =
        transaction {
            TaskSubtasks.selectAll().map { rowToTaskSubtask(it) }
        }

    override fun save(entity: TaskSubtaskDto): TaskSubtaskDto =
        transaction {
            if (findById(entity.id) == null) {
                TaskSubtasks.insert {
                    it[TaskSubtasks.id] = entity.id
                    it[TaskSubtasks.taskId] = entity.taskId
                    it[TaskSubtasks.subtaskId] = entity.subtaskId
                    it[TaskSubtasks.position] = entity.position
                }
            } else {
                TaskSubtasks.update({ TaskSubtasks.id eq entity.id }) {
                    it[TaskSubtasks.taskId] = entity.taskId
                    it[TaskSubtasks.subtaskId] = entity.subtaskId
                    it[TaskSubtasks.position] = entity.position
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean = transaction { TaskSubtasks.deleteWhere { TaskSubtasks.id eq id } > 0 }

    // Получить все подзадачи по задаче отсортированные по порядку
    fun getSubtasksForTask(taskId: UUID): List<SubTaskDto> =
        transaction {
            (TaskSubtasks innerJoin Subtasks)
                .selectAll().where { TaskSubtasks.taskId eq taskId }
                .orderBy(TaskSubtasks.position to SortOrder.ASC)
                .map { row -> SubtaskDao().rowToSubtask(row) }
        }

    private fun rowToTaskSubtask(row: ResultRow): TaskSubtaskDto =
        TaskSubtaskDto(
            id = row[TaskSubtasks.id],
            taskId = row[TaskSubtasks.taskId],
            subtaskId = row[TaskSubtasks.subtaskId],
            position = row[TaskSubtasks.position],
        )
}
