package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.Tasks
import su.itgalley.dto.TaskDto
import java.util.UUID

class TaskDao : BaseDao<TaskDto, UUID> {
    override fun findById(id: UUID): TaskDto? =
        transaction {
            Tasks
                .selectAll()
                .where { Tasks.id eq id }
                .map { rowToTask(it) }
                .singleOrNull()
        }

    override fun findAll(): List<TaskDto> =
        transaction {
            Tasks.selectAll().map { rowToTask(it) }
        }

    override fun save(entity: TaskDto): TaskDto =
        transaction {
            if (findById(entity.id) == null) {
                Tasks.insert {
                    it[Tasks.id] = entity.id
                    it[Tasks.description] = entity.description
                }
            } else {
                Tasks.update({ Tasks.id eq entity.id }) {
                    it[Tasks.description] = entity.description
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            Tasks.deleteWhere { Tasks.id eq id } > 0
        }

    private fun rowToTask(row: ResultRow): TaskDto =
        TaskDto(
            id = row[Tasks.id],
            description = row[Tasks.description],
        )
}
