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
import su.itgalley.database.schema.Levels
import su.itgalley.database.schema.Tasks
import su.itgalley.dto.LevelDto
import java.util.UUID

class LevelDao : BaseDao<LevelDto, UUID> {
    override fun findById(id: UUID): LevelDto? =
        transaction {
            Levels.select(Levels.id eq id)
                .map { rowToLevel(it) }
                .singleOrNull()
        }

    override fun findAll(): List<LevelDto> =
        transaction {
            Levels.selectAll().map { rowToLevel(it) }
        }

    override fun save(entity: LevelDto): LevelDto =
        transaction {
            if (findById(entity.id) == null) {
                Levels.insert {
                    it[Levels.id] = entity.id
                    it[Levels.name] = entity.name
                    it[Levels.blockId] = entity.blockId
                    it[Levels.position] = entity.position
                    it[Levels.tutorialId] = entity.tutorialId
                    it[Levels.taskId] = entity.taskId
                    it[Levels.levelHelpId] = entity.levelHelpId
                    it[Levels.requiredInBlock] = entity.requiredInBlock
                }
            } else {
                Levels.update({ Levels.id eq entity.id }) {
                    it[Levels.name] = entity.name
                    it[Levels.blockId] = entity.blockId
                    it[Levels.position] = entity.position
                    it[Levels.tutorialId] = entity.tutorialId
                    it[Levels.taskId] = entity.taskId
                    it[Levels.levelHelpId] = entity.levelHelpId
                    it[Levels.requiredInBlock] = entity.requiredInBlock
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean = transaction { Levels.deleteWhere { Levels.id eq id } > 0 }

    // Получить уровни блока, отсортированные по позиции
    fun findByBlockOrdered(blockId: UUID): List<LevelDto> =
        transaction {
            Levels.select(Levels.blockId eq blockId)
                .orderBy(Levels.position to SortOrder.ASC)
                .map { rowToLevel(it) }
        }

    private fun rowToLevel(row: ResultRow): LevelDto =
        LevelDto(
            id = row[Levels.id],
            name = row[Levels.name],
            blockId = row[Levels.blockId],
            position = row[Levels.position],
            tutorialId = row[Levels.tutorialId],
            taskId = row[Levels.taskId],
            levelHelpId = row[Levels.levelHelpId],
            requiredInBlock = row[Levels.requiredInBlock],
        )

    fun getLevelTaskDescription(levelId: UUID): String? =
        transaction {
            (Levels innerJoin Tasks)
                .select(Levels.id eq levelId)
                .map { it[Tasks.description] }
                .singleOrNull()
        }
}
