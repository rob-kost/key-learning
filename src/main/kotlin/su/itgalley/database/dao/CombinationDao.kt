package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import su.itgalley.database.schema.Combinations
import su.itgalley.dto.CombinationDto
import java.util.UUID

class CombinationDao : BaseDao<CombinationDto, UUID> {
    override fun findById(id: UUID): CombinationDto? =
        transaction {
            Combinations.selectAll().where { Combinations.id eq id }
                .map { rowToCombination(it) }
                .singleOrNull()
        }

    override fun findAll(): List<CombinationDto> =
        transaction {
            Combinations.selectAll().map { rowToCombination(it) }
        }

    override fun save(entity: CombinationDto): CombinationDto =
        transaction {
            if (findById(entity.id) == null) {
                Combinations.insert {
                    it[Combinations.id] = entity.id
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            Combinations.deleteWhere { Combinations.id eq id } > 0
        }

    private fun rowToCombination(row: ResultRow): CombinationDto =
        CombinationDto(
            id = row[Combinations.id],
        )
}
