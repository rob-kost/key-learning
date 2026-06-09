package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.LevelHelps
import su.itgalley.dto.LevelHelpDto
import java.util.UUID

class LevelHelpDao : BaseDao<LevelHelpDto, UUID> {
    override fun findById(id: UUID): LevelHelpDto? =
        transaction {
            LevelHelps.selectAll().where { LevelHelps.id eq id }
                .map { rowToLevelHelp(it) }
                .singleOrNull()
        }

    override fun findAll(): List<LevelHelpDto> =
        transaction {
            LevelHelps.selectAll().map { rowToLevelHelp(it) }
        }

    override fun save(entity: LevelHelpDto): LevelHelpDto =
        transaction {
            if (findById(entity.id) == null) {
                LevelHelps.insert {
                    it[LevelHelps.id] = entity.id
                    it[LevelHelps.content] = entity.content
                }
            } else {
                LevelHelps.update({ LevelHelps.id eq entity.id }) {
                    it[LevelHelps.content] = entity.content
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            LevelHelps.deleteWhere { LevelHelps.id eq id } > 0
        }

    private fun rowToLevelHelp(row: ResultRow): LevelHelpDto =
        LevelHelpDto(
            id = row[LevelHelps.id],
            content = row[LevelHelps.content],
        )
}
