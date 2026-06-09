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
import su.itgalley.database.schema.CombinationKeys
import su.itgalley.database.schema.KeysTable
import su.itgalley.dto.CombinationKeyDto
import java.util.UUID

class CombinationKeyDao : BaseDao<CombinationKeyDto, UUID> {
    override fun findById(id: UUID): CombinationKeyDto? =
        transaction {
            CombinationKeys.selectAll().where { CombinationKeys.id eq id }
                .map { rowToCombinationKey(it) }
                .singleOrNull()
        }

    override fun findAll(): List<CombinationKeyDto> =
        transaction {
            CombinationKeys.selectAll().map { rowToCombinationKey(it) }
        }

    override fun save(entity: CombinationKeyDto): CombinationKeyDto =
        transaction {
            if (findById(entity.id) == null) {
                CombinationKeys.insert {
                    it[CombinationKeys.id] = entity.id
                    it[CombinationKeys.combinationId] = entity.combinationId
                    it[CombinationKeys.keyId] = entity.keyId
                    it[CombinationKeys.position] = entity.position
                }
            } else {
                CombinationKeys.update({ CombinationKeys.id eq entity.id }) {
                    it[CombinationKeys.combinationId] = entity.combinationId
                    it[CombinationKeys.keyId] = entity.keyId
                    it[CombinationKeys.position] = entity.position
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            CombinationKeys.deleteWhere { CombinationKeys.id eq id } > 0
        }

    // Получить все клавиши комбинации, отсортированные по позиции
    fun getKeysForCombination(combinationId: UUID): List<CombinationKeyDto> =
        transaction {
            CombinationKeys.selectAll().where { CombinationKeys.combinationId eq combinationId }
                .orderBy(CombinationKeys.position to SortOrder.ASC)
                .map { rowToCombinationKey(it) }
        }

    // Удалить все связи для комбинации
    fun deleteByCombinationId(combinationId: UUID): Boolean =
        transaction {
            CombinationKeys.deleteWhere { CombinationKeys.combinationId eq combinationId } > 0
        }

    // Получить combinationId по клавише
    fun getCombinationIdsByKey(keyValue: String): List<UUID> =
        transaction {
            (CombinationKeys innerJoin KeysTable)
                .selectAll().where { KeysTable.key eq keyValue }
                .map { it[CombinationKeys.combinationId] }
                .distinct()
        }

    private fun rowToCombinationKey(row: ResultRow): CombinationKeyDto =
        CombinationKeyDto(
            id = row[CombinationKeys.id],
            combinationId = row[CombinationKeys.combinationId],
            keyId = row[CombinationKeys.keyId],
            position = row[CombinationKeys.position],
        )
}
