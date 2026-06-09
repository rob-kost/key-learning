package su.itgalley.database.dao

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import su.itgalley.database.schema.KeysTable
import su.itgalley.dto.KeysTableDto
import java.util.UUID

class KeyDao : BaseDao<KeysTableDto, UUID> {
    override fun findById(id: UUID): KeysTableDto? =
        transaction {
            KeysTable.selectAll().where { KeysTable.id eq id }
                .map { rowToKey(it) }
                .singleOrNull()
        }

    override fun findAll(): List<KeysTableDto> =
        transaction {
            KeysTable.selectAll().map { rowToKey(it) }
        }

    override fun save(entity: KeysTableDto): KeysTableDto =
        transaction {
            if (findById(entity.id) == null) {
                KeysTable.insert {
                    it[KeysTable.id] = entity.id
                    it[KeysTable.key] = entity.key
                    it[KeysTable.keyGroup] = entity.keyGroup
                }
            } else {
                KeysTable.update({ KeysTable.id eq entity.id }) {
                    it[KeysTable.key] = entity.key
                    it[KeysTable.keyGroup] = entity.keyGroup
                }
            }
            entity
        }

    override fun deleteById(id: UUID): Boolean =
        transaction {
            KeysTable.deleteWhere { KeysTable.id eq id } > 0
        }

    // Поиск клавиши по названию
    fun findByKeyName(keyName: String): KeysTableDto? =
        transaction {
            KeysTable.selectAll().where { KeysTable.key eq keyName }
                .map { rowToKey(it) }
                .singleOrNull()
        }

    private fun rowToKey(row: ResultRow): KeysTableDto =
        KeysTableDto(
            id = row[KeysTable.id],
            key = row[KeysTable.key],
            keyGroup = row[KeysTable.keyGroup],
        )
}
