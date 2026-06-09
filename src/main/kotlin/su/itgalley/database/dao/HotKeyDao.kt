package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import su.itgalley.database.schema.CombinationKeys
import su.itgalley.database.schema.HotKeys
import su.itgalley.database.schema.KeysTable
import su.itgalley.dto.HotKeyDto
import su.itgalley.dto.KeyWithPosition
import java.util.*

class HotKeyDao : BaseDao<HotKeyDto, UUID> {
    override fun findById(id: UUID): HotKeyDto? =
        transaction {
            HotKeys.selectAll().where { HotKeys.id eq id }
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

    private fun rowToHotKey(row: ResultRow): HotKeyDto =
        HotKeyDto(
            id = row[HotKeys.id],
            blockId = row[HotKeys.blockId],
            description = row[HotKeys.description],
            keyCombinationId = row[HotKeys.keyCombinationId],
        )

    fun getKeysForCombination(combinationId: UUID): List<KeyWithPosition> = transaction {
        (CombinationKeys innerJoin KeysTable)
            .selectAll().where { CombinationKeys.combinationId eq combinationId }
            .orderBy(CombinationKeys.position to SortOrder.ASC)
            .map { row ->
                KeyWithPosition(
                    key = row[KeysTable.key],
                    position = row[CombinationKeys.position],
                )
            }
    }
}
