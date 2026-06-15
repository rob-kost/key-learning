package su.itgalley.database.seed

import kotlinx.serialization.Serializable
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType

@Serializable
data class InputSubtask(
    val type: String,
    val desc: String,
    val solution: String,
)

@Serializable
data class InputLevel(
    val name: String,
    val tutorial: String?,
    val help: String?,
    val subtasks: List<InputSubtask>,
)

@Serializable
data class InputBlock(
    val name: String,
    val description: String,
    val levels: List<InputLevel>,
)

// Контейнер для накопления данных
private class SeedDataBuilder {
    val keySeeds = mutableListOf<KeySeed>()
    val combinationSeeds = mutableListOf<CombinationSeed>()
    val combinationKeySeeds = mutableListOf<CombinationKeySeed>()
    val hotKeySeeds = mutableListOf<HotKeySeed>()
    val blockSeeds = mutableListOf<BlockSeed>()
    val tutorialSeeds = mutableListOf<TutorialSeed>()
    val helpSeeds = mutableListOf<LevelHelpSeed>()
    val taskSeeds = mutableListOf<TaskSeed>()
    val subtaskSeeds = mutableListOf<SubtaskSeed>()
    val taskSubtaskSeeds = mutableListOf<TaskSubtaskSeed>()
    val levelSeeds = mutableListOf<LevelSeed>()
}

fun convertBlocksToSeedData(blocks: List<InputBlock>): SeedData {
    val builder = SeedDataBuilder()
    val keyRefMap = mutableMapOf<String, String>()
    val combinationRefMap = mutableMapOf<String, String>()
    val hotKeyRefMap = mutableMapOf<String, String>()

    fun getOrCreateKeyRef(keyName: String): String {
        return keyRefMap.getOrPut(keyName) {
            val controlKeys =
                setOf(
                    "Control", "Shift", "Alt", "Meta",
                    "Home", "End", "PageUp", "PageDown",
                    "Insert", "Delete", "Backspace",
                    "Tab", "Enter", "Escape",
                    "ArrowUp", "ArrowDown", "ArrowLeft", "ArrowRight",
                    "CapsLock", "NumLock", "ScrollLock",
                    "Pause", "PrintScreen", "ContextMenu",
                    "F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10", "F11", "F12",
                )
            val group = if (keyName in controlKeys) KeyGroup.CONTROLS else KeyGroup.SYMBOLS
            builder.keySeeds.add(KeySeed(keyName, group))
            keyName
        }
    }

    fun getOrCreateCombinationRef(combinationStr: String): String {
        return combinationRefMap.getOrPut(combinationStr) {
            val combRef = "comb_${combinationStr.replace("+", "_").replace(" ", "")}"
            builder.combinationSeeds.add(CombinationSeed(combRef))
            val keyNames = combinationStr.split('+').map { it.trim() }
            keyNames.forEachIndexed { idx, keyName ->
                val keyRef = getOrCreateKeyRef(keyName)
                builder.combinationKeySeeds.add(CombinationKeySeed(combRef, keyRef, idx + 1))
            }
            combRef
        }
    }

    fun getOrCreateHotKeyRef(
        combinationStr: String,
        blockRef: String,
    ): String {
        val uniqueKey = "$combinationStr@$blockRef"
        return hotKeyRefMap.getOrPut(uniqueKey) {
            val hkRef = "hk_${combinationStr.replace("+", "_").replace(" ", "")}_$blockRef"
            val combRef = getOrCreateCombinationRef(combinationStr)
            builder.hotKeySeeds.add(HotKeySeed(hkRef, blockRef, combinationStr, combRef))
            hkRef
        }
    }

    // Создание всех подзадач уровня
    fun createSubtasks(
        levelKey: String,
        subtasks: List<InputSubtask>,
        blockRef: String,
        hotKeyCreator: (String, String) -> String,
    ): List<String> {
        val subtaskRefs = mutableListOf<String>()
        subtasks.forEachIndexed { subIdx, sub ->
            val subRef = "sub_${levelKey}_$subIdx"
            subtaskRefs.add(subRef)
            when (sub.type) {
                "TYPING" -> {
                    builder.subtaskSeeds.add(
                        SubtaskSeed(
                            ref = subRef,
                            description = sub.desc,
                            solutionType = SolutionType.TYPING,
                            stringSolution = sub.solution,
                            keySolutionRef = null,
                        ),
                    )
                }
                "HOTKEY" -> {
                    val hotKeyRef = hotKeyCreator(sub.solution, blockRef)
                    builder.subtaskSeeds.add(
                        SubtaskSeed(
                            ref = subRef,
                            description = sub.desc,
                            solutionType = SolutionType.HOTKEY,
                            stringSolution = null,
                            keySolutionRef = hotKeyRef,
                        ),
                    )
                }
                else -> error("Unknown subtask type: ${sub.type}")
            }
        }
        return subtaskRefs
    }

    // Обработка одного уровня
    fun processLevel(
        blockRef: String,
        level: InputLevel,
        levelIdx: Int,
        hotKeyCreator: (String, String) -> String,
    ) {
        val levelKey = "${blockRef}_${level.name.replace(" ", "_").lowercase()}"
        val taskRef = "task_$levelKey"

        val tutorialRef =
            level.tutorial?.let {
                val tutRef = "tut_$levelKey"
                builder.tutorialSeeds.add(TutorialSeed(tutRef, it))
                tutRef
            }

        val helpRef =
            level.help?.let {
                val hpRef = "help_$levelKey"
                builder.helpSeeds.add(LevelHelpSeed(hpRef, it))
                hpRef
            }

        builder.taskSeeds.add(TaskSeed(taskRef, level.name))

        val subtaskRefs = createSubtasks(levelKey, level.subtasks, blockRef, hotKeyCreator)
        subtaskRefs.forEachIndexed { pos, subRef ->
            builder.taskSubtaskSeeds.add(TaskSubtaskSeed(taskRef, subRef, pos + 1))
        }

        builder.levelSeeds.add(
            LevelSeed(
                ref = "level_$levelKey",
                name = level.name,
                blockRef = blockRef,
                position = levelIdx + 1,
                tutorialRef = tutorialRef,
                taskRef = taskRef,
                levelHelpRef = helpRef,
                requiredInBlock = RequiredInBlock.NO,
            ),
        )
    }

    // Основной цикл по блокам
    blocks.forEach { block ->
        val blockRef = block.name.replace(" ", "_").lowercase()
        builder.blockSeeds.add(BlockSeed(blockRef, block.name, block.description))
        block.levels.forEachIndexed { idx, level ->
            processLevel(blockRef, level, idx, ::getOrCreateHotKeyRef)
        }
    }

    return SeedData(
        keys = builder.keySeeds,
        blocks = builder.blockSeeds,
        tutorials = builder.tutorialSeeds,
        levelHelps = builder.helpSeeds,
        tasks = builder.taskSeeds,
        combinations = builder.combinationSeeds,
        combinationKeys = builder.combinationKeySeeds,
        hotKeys = builder.hotKeySeeds,
        subtasks = builder.subtaskSeeds,
        taskSubtasks = builder.taskSubtaskSeeds,
        levels = builder.levelSeeds,
    )
}
