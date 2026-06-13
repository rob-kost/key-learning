package su.itgalley.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.core.or
import java.util.UUID

enum class KeyGroup { CONTROLS, SYMBOLS }

enum class RequiredInBlock { YES, NO }

enum class SolutionType { HOTKEY, TYPING }

object Blocks : Table("blocks") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val name = varchar("name", 128).uniqueIndex()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Combinations : Table("combinations") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }

    override val primaryKey = PrimaryKey(id)
}

object HotKeys : Table("hotkeys") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val blockId = reference("block_id", Blocks.id, onDelete = ReferenceOption.CASCADE)
    val description = text("description")
    val keyCombinationId = reference("key_combination_id", Combinations.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_block_combination", blockId, keyCombinationId)
        uniqueIndex("uk_block_description", blockId, description)
    }
}

object KeysTable : Table("keys") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val key = varchar("key", 64).uniqueIndex()
    val keyGroup = enumerationByName("key_group", 50, KeyGroup::class)

    override val primaryKey = PrimaryKey(id)
}

object CombinationKeys : Table("combinationkeys") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val combinationId = reference("combination_id", Combinations.id, onDelete = ReferenceOption.CASCADE)
    val keyId = reference("key_id", KeysTable.id, onDelete = ReferenceOption.RESTRICT)
    val position = integer("position")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_position", combinationId, position)
        uniqueIndex("uk_combination_key", combinationId, keyId)
    }
}

object Tutorials : Table("tutorials") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table("tasks") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val description = text("description")

    override val primaryKey = PrimaryKey(id)
}

object LevelHelps : Table("levelhelps") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}

object Levels : Table("levels") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val name = varchar("name", 128)
    val blockId = reference("block_id", Blocks.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val tutorialId = reference("tutorial_id", Tutorials.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val levelHelpId = reference("level_help_id", LevelHelps.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val requiredInBlock = enumerationByName("required_in_block", 10, RequiredInBlock::class).default(RequiredInBlock.NO)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_block_position", blockId, position)
        uniqueIndex("uk_block_name", blockId, name)
    }
}

object Subtasks : Table("subtasks") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val description = text("description")
    val solutionType = enumerationByName("solution_type", 20, SolutionType::class)
    val stringSolution = text("string_solution").nullable()
    val keySolutionId = reference("key_solution_id", HotKeys.id, onDelete = ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        check("chk_subtask_solution") {
            (solutionType eq SolutionType.TYPING and stringSolution.isNotNull()) or
                (solutionType eq SolutionType.HOTKEY and keySolutionId.isNotNull())
        }
        check("chk_subtask_clean_other_solution") {
            (solutionType eq SolutionType.TYPING and keySolutionId.isNull()) and
                (solutionType eq SolutionType.HOTKEY and stringSolution.isNull())
        }
    }
}

object TaskSubtasks : Table("tasksubtasks") {
    val id = javaUUID("id").clientDefault { UUID.randomUUID() }
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val subtaskId = reference("subtask_id", Subtasks.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_task_position", taskId, position)
    }
}
