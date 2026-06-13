package su.itgalley.database.seed

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import su.itgalley.database.dao.DaoRegistry
import su.itgalley.dto.BlockDto
import su.itgalley.dto.CombinationDto
import su.itgalley.dto.CombinationKeyDto
import su.itgalley.dto.HotKeyDto
import su.itgalley.dto.KeysTableDto
import su.itgalley.dto.LevelDto
import su.itgalley.dto.LevelHelpDto
import su.itgalley.dto.SubTaskDto
import su.itgalley.dto.TaskDto
import su.itgalley.dto.TaskSubtaskDto
import su.itgalley.dto.TutorialDto
import java.util.UUID
import kotlin.collections.set

private class RefMaps {
    val keyRefToId = mutableMapOf<String, UUID>()
    val combinationRefToId = mutableMapOf<String, UUID>()
    val blockRefToId = mutableMapOf<String, UUID>()
    val tutorialRefToId = mutableMapOf<String, UUID>()
    val helpRefToId = mutableMapOf<String, UUID>()
    val taskRefToId = mutableMapOf<String, UUID>()
    val hotKeyRefToId = mutableMapOf<String, UUID>()
    val subtaskRefToId = mutableMapOf<String, UUID>()
}

@Suppress("TooManyFunctions")
class DatabaseSeeder(
    private val dao: DaoRegistry,
) {
    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
    }

    fun seed() {
        if (dao.blockDao.findAll().isNotEmpty()) {
            println("Database already seeded, skipping")
            return
        }

        val seedData = loadSeedData()
        val refs = RefMaps()

        transaction {
            insertKeys(seedData.keys, refs)
            insertBlocks(seedData.blocks, refs)
            insertTutorials(seedData.tutorials, refs)
            insertLevelHelps(seedData.levelHelps, refs)
            insertTasks(seedData.tasks, refs)
            insertCombinations(seedData.combinations, refs)
            insertCombinationKeys(seedData.combinationKeys, refs)
            insertHotKeys(seedData.hotKeys, refs)
            insertSubtasks(seedData.subtasks, refs)
            insertTaskSubtasks(seedData.taskSubtasks, refs)
            insertLevels(seedData.levels, refs)
        }

        println("Seeding completed successfully")
    }

    private fun loadSeedData(): SeedData {
        val jsonString =
            this::class.java.getResource("/seed_data.json")?.readText()
                ?: error("seed_data.json not found in resources")
        return json.decodeFromString(jsonString)
    }

    private fun insertKeys(
        keys: List<KeySeed>,
        refs: RefMaps,
    ) {
        keys.forEach { key ->
            val id = UUID.randomUUID()
            refs.keyRefToId[key.key] = id
            dao.keyDao.save(KeysTableDto(id, key.key, key.keyGroup))
        }
    }

    private fun insertBlocks(
        blocks: List<BlockSeed>,
        refs: RefMaps,
    ) {
        blocks.forEach { block ->
            val id = UUID.randomUUID()
            refs.blockRefToId[block.ref] = id
            dao.blockDao.save(BlockDto(id, block.name, block.description))
        }
    }

    private fun insertTutorials(
        tutorials: List<TutorialSeed>,
        refs: RefMaps,
    ) {
        tutorials.forEach { tut ->
            val id = UUID.randomUUID()
            refs.tutorialRefToId[tut.ref] = id
            dao.tutorialDao.save(TutorialDto(id, tut.content))
        }
    }

    private fun insertLevelHelps(
        helps: List<LevelHelpSeed>,
        refs: RefMaps,
    ) {
        helps.forEach { help ->
            val id = UUID.randomUUID()
            refs.helpRefToId[help.ref] = id
            dao.levelHelpDao.save(LevelHelpDto(id, help.content))
        }
    }

    private fun insertTasks(
        tasks: List<TaskSeed>,
        refs: RefMaps,
    ) {
        tasks.forEach { task ->
            val id = UUID.randomUUID()
            refs.taskRefToId[task.ref] = id
            dao.taskDao.save(TaskDto(id, task.description))
        }
    }

    private fun insertCombinations(
        combinations: List<CombinationSeed>,
        refs: RefMaps,
    ) {
        combinations.forEach { comb ->
            val id = UUID.randomUUID()
            refs.combinationRefToId[comb.ref] = id
            dao.combinationDao.save(CombinationDto(id))
        }
    }

    private fun insertCombinationKeys(
        keys: List<CombinationKeySeed>,
        refs: RefMaps,
    ) {
        keys.forEach { ck ->
            val combinationId =
                refs.combinationRefToId[ck.combinationRef]
                    ?: error("Combination ref ${ck.combinationRef} not found")
            val keyId =
                refs.keyRefToId[ck.keyRef]
                    ?: error("Key ref ${ck.keyRef} not found")
            dao.combinationKeyDao.save(
                CombinationKeyDto(
                    id = UUID.randomUUID(),
                    combinationId = combinationId,
                    keyId = keyId,
                    position = ck.position,
                ),
            )
        }
    }

    private fun insertHotKeys(
        hotKeys: List<HotKeySeed>,
        refs: RefMaps,
    ) {
        hotKeys.forEach { hk ->
            val blockId =
                refs.blockRefToId[hk.blockRef]
                    ?: error("Block ref ${hk.blockRef} not found")
            val combinationId =
                refs.combinationRefToId[hk.combinationRef]
                    ?: error("Combination ref ${hk.combinationRef} not found")
            val id = UUID.randomUUID()
            refs.hotKeyRefToId[hk.ref] = id
            dao.hotKeyDao.save(HotKeyDto(id, blockId, hk.description, combinationId))
        }
    }

    private fun insertSubtasks(
        subtasks: List<SubtaskSeed>,
        refs: RefMaps,
    ) {
        subtasks.forEach { sub ->
            val keySolutionId =
                sub.keySolutionRef?.let { ref ->
                    refs.hotKeyRefToId[ref] ?: error("HotKey ref $ref not found")
                }
            val id = UUID.randomUUID()
            refs.subtaskRefToId[sub.ref] = id
            dao.subtaskDao.save(
                SubTaskDto(
                    id = id,
                    description = sub.description,
                    solutionType = sub.solutionType,
                    stringSolution = sub.stringSolution,
                    keySolutionId = keySolutionId,
                ),
            )
        }
    }

    private fun insertTaskSubtasks(
        taskSubtasks: List<TaskSubtaskSeed>,
        refs: RefMaps,
    ) {
        taskSubtasks.forEach { ts ->
            val taskId =
                refs.taskRefToId[ts.taskRef]
                    ?: error("Task ref ${ts.taskRef} not found")
            val subtaskId =
                refs.subtaskRefToId[ts.subtaskRef]
                    ?: error("Subtask ref ${ts.subtaskRef} not found")
            dao.taskSubtaskDao.save(
                TaskSubtaskDto(
                    id = UUID.randomUUID(),
                    taskId = taskId,
                    subtaskId = subtaskId,
                    position = ts.position,
                ),
            )
        }
    }

    private fun insertLevels(
        levels: List<LevelSeed>,
        refs: RefMaps,
    ) {
        levels.forEach { level ->
            val blockId =
                refs.blockRefToId[level.blockRef]
                    ?: error("Block ref ${level.blockRef} not found")
            val tutorialId = level.tutorialRef?.let { refs.tutorialRefToId[it] }
            val taskId =
                refs.taskRefToId[level.taskRef]
                    ?: error("Task ref ${level.taskRef} not found")
            val levelHelpId = level.levelHelpRef?.let { refs.helpRefToId[it] }
            dao.levelDao.save(
                LevelDto(
                    id = UUID.randomUUID(),
                    name = level.name,
                    blockId = blockId,
                    position = level.position,
                    tutorialId = tutorialId,
                    taskId = taskId,
                    levelHelpId = levelHelpId,
                    requiredInBlock = level.requiredInBlock,
                ),
            )
        }
    }
}
