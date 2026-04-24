@file:OptIn(ExperimentalUuidApi::class)

package su.itgalley.database.schema

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.core.isNull
import org.jetbrains.exposed.v1.core.or
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Suppress("EnumNaming", "ktlint:standard:enum-entry-name-case")
enum class KeyGroup { `управляющие клавиши`, `печатные клавиши` }

@Suppress("EnumNaming", "ktlint:standard:enum-entry-name-case")
enum class RequiredInBlock { Yes, No }

@Suppress("EnumNaming", "ktlint:standard:enum-entry-name-case")
enum class SolutionType { hotkey, typing }

object Blocks : Table("blocks") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val name = varchar("name", 128).uniqueIndex()
    val description = text("description").nullable()

    override val primaryKey = PrimaryKey(id)
}

object Combinations : Table("combinations") {
    val id = uuid("id").clientDefault { Uuid.random() }

    override val primaryKey = PrimaryKey(id)
}

object HotKeys : Table("hotkeys") {
    val id = uuid("id").clientDefault { Uuid.random() }
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
    val id = uuid("id").clientDefault { Uuid.random() }
    val key = varchar("key", 64).uniqueIndex()
    val keyGroup = enumerationByName("key_group", 50, KeyGroup::class)

    override val primaryKey = PrimaryKey(id)
}

object CombinationKeys : Table("combination_keys") {
    val id = uuid("id").clientDefault { Uuid.random() }
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
    val id = uuid("id").clientDefault { Uuid.random() }
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}

object Tasks : Table("tasks") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val description = text("description")

    override val primaryKey = PrimaryKey(id)
}

object LevelHelps : Table("level_helps") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val content = text("content")

    override val primaryKey = PrimaryKey(id)
}

object Levels : Table("levels") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val name = varchar("name", 128)
    val blockId = reference("block_id", Blocks.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val tutorialId = reference("tutorial_id", Tutorials.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val levelHelpId = reference("level_help_id", LevelHelps.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val requiredInBlock = enumerationByName("required_in_block", 10, RequiredInBlock::class).default(RequiredInBlock.No)

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_block_position", blockId, position)
        uniqueIndex("uk_block_name", blockId, name)
    }
}

object Subtasks : Table("subtasks") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val description = text("description")
    val solutionType = enumerationByName("solution_type", 20, SolutionType::class)
    val stringSolution = text("string_solution").nullable()
    val keySolutionId = reference("key_solution_id", HotKeys.id, onDelete = ReferenceOption.CASCADE).nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        check("chk_subtask_solution") {
            (solutionType eq SolutionType.typing and stringSolution.isNotNull()) or
                (solutionType eq SolutionType.hotkey and keySolutionId.isNotNull())
        }
        check("chk_subtask_clean_other_solution") {
            (solutionType eq SolutionType.typing and keySolutionId.isNull()) and
                (solutionType eq SolutionType.hotkey and stringSolution.isNull())
        }
    }
}

object TaskSubtasks : Table("task_subtasks") {
    val id = uuid("id").clientDefault { Uuid.random() }
    val taskId = reference("task_id", Tasks.id, onDelete = ReferenceOption.CASCADE)
    val subtaskId = reference("subtask_id", Subtasks.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex("uk_task_position", taskId, position)
    }
}
