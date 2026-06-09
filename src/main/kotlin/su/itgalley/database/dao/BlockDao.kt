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
import su.itgalley.database.schema.Blocks
import su.itgalley.dto.BlockDto
import java.util.UUID

class BlockDao : BaseDao<BlockDto, UUID> {
    override fun findById(id: UUID): BlockDto? =
        transaction {
            Blocks.select(Blocks.id eq id)
                .map { rowToBlock(it) }
                .singleOrNull()
        }

    override fun findAll(): List<BlockDto> =
        transaction {
            Blocks.selectAll()
                .map { rowToBlock(it) }
        }

    fun findAllSorted(): List<BlockDto> =
        transaction {
            Blocks.selectAll()
                .orderBy(Blocks.name to SortOrder.ASC)   // добавляем сортировку
                .map { rowToBlock(it) }
        }

    override fun save(entity: BlockDto): BlockDto =
        transaction {
            val id = entity.id
            if (findById(id) == null) {
                Blocks.insert {
                    it[Blocks.id] = id
                    it[Blocks.name] = entity.name
                    it[Blocks.description] = entity.description
                }
            } else {
                Blocks.update({ Blocks.id eq id }) {
                    it[Blocks.name] = entity.name
                    it[Blocks.description] = entity.description
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            Blocks.deleteWhere { Blocks.id eq id } > 0
        }

    fun findByName(name: String): BlockDto? =
        transaction {
            Blocks.select(Blocks.name eq name)
                .map { rowToBlock(it) }
                .singleOrNull()
        }

    private fun rowToBlock(row: ResultRow): BlockDto =
        BlockDto(
            id = row[Blocks.id],
            name = row[Blocks.name],
            description = row[Blocks.description] ?: "",
        )
}
