package su.itgalley.support

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import su.itgalley.database.schema.Blocks
import su.itgalley.database.schema.CombinationKeys
import su.itgalley.database.schema.Combinations
import su.itgalley.database.schema.HotKeys
import su.itgalley.database.schema.KeysTable
import su.itgalley.database.schema.LevelHelps
import su.itgalley.database.schema.Levels
import su.itgalley.database.schema.Subtasks
import su.itgalley.database.schema.TaskSubtasks
import su.itgalley.database.schema.Tasks
import su.itgalley.database.schema.Tutorials

object TestDatabase {
    private val tables =
        arrayOf(
            Blocks,
            Combinations,
            KeysTable,
            CombinationKeys,
            HotKeys,
            Tutorials,
            Tasks,
            LevelHelps,
            Levels,
            Subtasks,
            TaskSubtasks,
        )

    private var initialized = false

    fun init() {
        if (initialized) return

        Database.connect(
            url = "jdbc:h2:mem:keylearning_test;" +
                    "MODE=MySQL;" +
                    "DATABASE_TO_LOWER=TRUE;" +
                    "CASE_INSENSITIVE_IDENTIFIERS=TRUE;" +
                    "DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = "",
        )
        initialized = true
        createSchema()
    }

    fun reset() {
        transaction {
            SchemaUtils.drop(
                TaskSubtasks,
                Subtasks,
                Levels,
                LevelHelps,
                Tasks,
                Tutorials,
                HotKeys,
                CombinationKeys,
                KeysTable,
                Combinations,
                Blocks,
            )
            SchemaUtils.create(*tables)
        }
    }

    private fun createSchema() {
        transaction {
            SchemaUtils.create(*tables)
        }
    }
}
