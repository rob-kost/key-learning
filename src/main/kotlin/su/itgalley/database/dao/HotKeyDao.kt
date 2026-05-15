package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.HotKeys
import su.itgalley.database.schema.Subtasks
import su.itgalley.database.schema.TaskSubtasks
import su.itgalley.dto.HotKeyDto
import java.util.UUID

class HotKeyDao : BaseDao<HotKeyDto, UUID> {
    override fun findById(id: UUID): HotKeyDto? =
        transaction {
            HotKeys.select(HotKeys.id eq id)
                .map { rowToHotKey(it) }
                .singleOrNull()
        }

    override fun findAll(): List<HotKeyDto> =
        transaction {
            HotKeys.selectAll().map { rowToHotKey(it) }
        }

    override fun save(entity: HotKeyDto): HotKeyDto =
        transaction {
            if (findById(entity.id) == null) {
                HotKeys.insert {
                    it[HotKeys.id] = entity.id
                    it[HotKeys.blockId] = entity.blockId
                    it[HotKeys.description] = entity.description
                    it[HotKeys.keyCombinationId] = entity.keyCombinationId
                }
            } else {
                HotKeys.update({ HotKeys.id eq entity.id }) {
                    it[HotKeys.blockId] = entity.blockId
                    it[HotKeys.description] = entity.description
                    it[HotKeys.keyCombinationId] = entity.keyCombinationId
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean = transaction { HotKeys.deleteWhere { HotKeys.id eq id } > 0 }

    // Получить все горячие клавиши блока
    fun findByBlock(blockId: UUID): List<HotKeyDto> =
        transaction {
            HotKeys.select(HotKeys.blockId eq blockId)
                .map { rowToHotKey(it) }
        }

    // Получить список хоткеев из подзадач для конкретной задачи
    fun getHotkeysByTask(taskId: UUID): List<HotKeyDto> =
        transaction {
            (
                HotKeys
                    .innerJoin(Subtasks).innerJoin(TaskSubtasks)
            )
                .select(TaskSubtasks.taskId eq taskId)
                .withDistinct()
                .map { rowToHotKey(it) }
        }

    private fun rowToHotKey(row: ResultRow): HotKeyDto =
        HotKeyDto(
            id = row[HotKeys.id],
            blockId = row[HotKeys.blockId],
            description = row[HotKeys.description],
            keyCombinationId = row[HotKeys.keyCombinationId],
        )
}
