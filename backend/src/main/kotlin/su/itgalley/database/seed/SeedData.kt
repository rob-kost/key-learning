package su.itgalley.database.seed

import kotlinx.serialization.Serializable
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType

@Serializable
data class SeedData(
    val keys: List<KeySeed>,
    val blocks: List<BlockSeed>,
    val tutorials: List<TutorialSeed>,
    val levelHelps: List<LevelHelpSeed>,
    val tasks: List<TaskSeed>,
    val combinations: List<CombinationSeed>,
    val combinationKeys: List<CombinationKeySeed>,
    val hotKeys: List<HotKeySeed>,
    val subtasks: List<SubtaskSeed>,
    val taskSubtasks: List<TaskSubtaskSeed>,
    val levels: List<LevelSeed>,
)

@Serializable
data class KeySeed(val key: String, val keyGroup: KeyGroup)

@Serializable
data class BlockSeed(val ref: String, val name: String, val description: String)

@Serializable
data class TutorialSeed(val ref: String, val content: String)

@Serializable
data class LevelHelpSeed(val ref: String, val content: String)

@Serializable
data class TaskSeed(val ref: String, val description: String)

@Serializable
data class CombinationSeed(val ref: String)

@Serializable
data class CombinationKeySeed(
    val combinationRef: String,
    val keyRef: String,
    val position: Int,
)

@Serializable
data class HotKeySeed(
    val ref: String,
    val blockRef: String,
    val description: String,
    val combinationRef: String,
)

@Serializable
data class SubtaskSeed(
    val ref: String,
    val description: String,
    val solutionType: SolutionType,
    val stringSolution: String?,
    val keySolutionRef: String? = null,
)

@Serializable
data class TaskSubtaskSeed(
    val taskRef: String,
    val subtaskRef: String,
    val position: Int,
)

@Serializable
data class LevelSeed(
    val ref: String,
    val name: String,
    val blockRef: String,
    val position: Int,
    val tutorialRef: String?,
    val taskRef: String,
    val levelHelpRef: String?,
    val requiredInBlock: RequiredInBlock,
)
