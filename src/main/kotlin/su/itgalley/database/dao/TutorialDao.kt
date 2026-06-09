package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.Tutorials
import su.itgalley.dto.TutorialDto
import java.util.UUID

class TutorialDao : BaseDao<TutorialDto, UUID> {
    override fun findById(id: UUID): TutorialDto? =
        transaction {
            Tutorials.selectAll().where { Tutorials.id eq id }
                .map { rowToTutorial(it) }
                .singleOrNull()
        }

    override fun findAll(): List<TutorialDto> =
        transaction {
            Tutorials.selectAll().map { rowToTutorial(it) }
        }

    override fun save(entity: TutorialDto): TutorialDto =
        transaction {
            if (findById(entity.id) == null) {
                Tutorials.insert {
                    it[Tutorials.id] = entity.id
                    it[Tutorials.content] = entity.content
                }
            } else {
                Tutorials.update({ Tutorials.id eq entity.id }) {
                    it[Tutorials.content] = entity.content
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            Tutorials.deleteWhere { Tutorials.id eq id } > 0
        }

    private fun rowToTutorial(row: ResultRow): TutorialDto =
        TutorialDto(
            id = row[Tutorials.id],
            content = row[Tutorials.content],
        )
}
