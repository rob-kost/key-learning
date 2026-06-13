package su.itgalley.database.dao

interface BaseDao<T, ID> {
    fun findById(id: ID): T?

    fun findAll(): List<T>

    fun save(entity: T): T

    fun deleteById(id: ID): Boolean
}
