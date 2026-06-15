package su.itgalley.database.dao

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import su.itgalley.support.DatabaseTestBase
import su.itgalley.support.TestFixtures
import java.util.UUID

class CombinationKeyDaoTest : DatabaseTestBase() {
    private val dao = CombinationKeyDao()
    private val registry = TestFixtures.createDaoRegistry()

    @Test
    fun `save find update delete and query combination keys`() {
        val graph = TestFixtures.seedLevelGraph(registry)
        val id = UUID.randomUUID()
        val extraKeyId = UUID.randomUUID()

        registry.keyDao.save(
            su.itgalley.dto.KeysTableDto(extraKeyId, "Shift", su.itgalley.database.schema.KeyGroup.CONTROLS),
        )
        dao.save(
            su.itgalley.dto.CombinationKeyDto(
                id = id,
                combinationId = graph.combinationId,
                keyId = extraKeyId,
                position = 3,
            ),
        )

        dao.findById(id)?.position shouldBe 3
        dao.getKeysForCombination(graph.combinationId) shouldHaveSize 3
        dao.getCombinationIdsByKey("Control") shouldContainExactly listOf(graph.combinationId)

        dao.save(
            su.itgalley.dto.CombinationKeyDto(
                id = id,
                combinationId = graph.combinationId,
                keyId = extraKeyId,
                position = 4,
            ),
        )
        dao.findById(id)?.position shouldBe 4

        dao.deleteByCombinationId(graph.combinationId) shouldBe true
        dao.getKeysForCombination(graph.combinationId) shouldHaveSize 0
    }
}

class HotKeyDaoTest : DatabaseTestBase() {
    private val dao = HotKeyDao()
    private val registry = TestFixtures.createDaoRegistry()

    @Test
    fun `save find update delete hotkey and load combination keys`() {
        val graph = TestFixtures.seedLevelGraph(registry)

        dao.findById(graph.hotKeyId)?.description shouldBe "Control+C"
        dao.getKeysForCombination(graph.combinationId).map { it.key } shouldContainExactly
            listOf("Control", "C")

        dao.save(
            su.itgalley.dto.HotKeyDto(
                id = graph.hotKeyId,
                blockId = graph.blockId,
                description = "Copy",
                keyCombinationId = graph.combinationId,
            ),
        )
        dao.findById(graph.hotKeyId)?.description shouldBe "Copy"

        dao.deleteById(graph.hotKeyId) shouldBe true
        dao.findById(graph.hotKeyId) shouldBe null
    }
}

class LevelDaoTest : DatabaseTestBase() {
    private val dao = LevelDao()
    private val registry = TestFixtures.createDaoRegistry()

    @Test
    fun `save find update delete level and query by block`() {
        val graph = TestFixtures.seedLevelGraph(registry)

        dao.findById(graph.levelId)?.name shouldBe "Level 1"
        dao.findByBlockOrdered(graph.blockId).single().id shouldBe graph.levelId
        dao.getLevelTaskDescription(graph.levelId) shouldBe "Practice task"

        dao.save(
            su.itgalley.dto.LevelDto(
                id = graph.levelId,
                name = "Updated level",
                blockId = graph.blockId,
                position = 1,
                tutorialId = graph.tutorialId,
                taskId = graph.taskId,
                levelHelpId = graph.helpId,
                requiredInBlock = su.itgalley.database.schema.RequiredInBlock.NO,
            ),
        )
        dao.findById(graph.levelId)?.name shouldBe "Updated level"

        dao.deleteById(graph.levelId) shouldBe true
        dao.findById(graph.levelId) shouldBe null
    }

    @Test
    fun `findByBlockOrdered sorts levels by position`() {
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        registry.blockDao.save(su.itgalley.dto.BlockDto(blockId, "Sorted", ""))
        registry.taskDao.save(su.itgalley.dto.TaskDto(taskId, "Task"))

        val levelTwo =
            su.itgalley.dto.LevelDto(
                id = UUID.randomUUID(),
                name = "Second",
                blockId = blockId,
                position = 2,
                tutorialId = null,
                taskId = taskId,
                levelHelpId = null,
                requiredInBlock = su.itgalley.database.schema.RequiredInBlock.NO,
            )
        val levelOne =
            su.itgalley.dto.LevelDto(
                id = UUID.randomUUID(),
                name = "First",
                blockId = blockId,
                position = 1,
                tutorialId = null,
                taskId = taskId,
                levelHelpId = null,
                requiredInBlock = su.itgalley.database.schema.RequiredInBlock.NO,
            )

        dao.save(levelTwo)
        dao.save(levelOne)

        dao.findByBlockOrdered(blockId).map { it.name } shouldContainExactly listOf("First", "Second")
    }
}

class SubtaskDaoTest : DatabaseTestBase() {
    private val dao = SubtaskDao()
    private val registry = TestFixtures.createDaoRegistry()

    @Test
    fun `query subtasks by level task and block with positions`() {
        val graph = TestFixtures.seedLevelGraph(registry)

        dao.findById(graph.typingSubtaskId)?.solutionType shouldBe
            su.itgalley.database.schema.SolutionType.TYPING
        dao.getSubtasksByLevel(graph.levelId).map { it.id } shouldContainExactly
            listOf(graph.typingSubtaskId, graph.hotkeySubtaskId)
        dao.getSubtasksByTask(graph.taskId).map { it.id } shouldContainExactly
            listOf(graph.typingSubtaskId, graph.hotkeySubtaskId)
        dao.getSubtasksByBlock(graph.blockId).map { it.id } shouldContainExactly
            listOf(graph.typingSubtaskId, graph.hotkeySubtaskId)

        val withPosition = dao.getSubtasksByLevelWithPosition(graph.levelId)
        withPosition shouldHaveSize 2
        val positions = withPosition.associate { it.first.id to it.second }
        positions[graph.typingSubtaskId] shouldBe 1
        positions[graph.hotkeySubtaskId] shouldBe 2
    }

    @Test
    fun `save update and delete subtask`() {
        val graph = TestFixtures.seedLevelGraph(registry)

        dao.save(
            su.itgalley.dto.SubTaskDto(
                id = graph.typingSubtaskId,
                description = "Updated typing",
                solutionType = su.itgalley.database.schema.SolutionType.TYPING,
                stringSolution = "world",
                keySolutionId = null,
            ),
        )
        dao.findById(graph.typingSubtaskId)?.description shouldBe "Updated typing"

        dao.deleteById(graph.typingSubtaskId) shouldBe true
        dao.findById(graph.typingSubtaskId) shouldBe null
    }
}

class TaskSubtaskDaoTest : DatabaseTestBase() {
    private val dao = TaskSubtaskDao()
    private val registry = TestFixtures.createDaoRegistry()

    @Test
    fun `save find update delete task subtask link and load subtasks`() {
        val graph = TestFixtures.seedLevelGraph(registry)
        val links = dao.findAll()
        val linkId = links.first().id

        dao.findById(linkId)?.taskId shouldBe graph.taskId
        dao.getSubtasksForTask(graph.taskId).map { it.id } shouldContainExactly
            listOf(graph.typingSubtaskId, graph.hotkeySubtaskId)

        dao.save(
            su.itgalley.dto.TaskSubtaskDto(
                id = linkId,
                taskId = graph.taskId,
                subtaskId = graph.typingSubtaskId,
                position = 10,
            ),
        )
        dao.findById(linkId)?.position shouldBe 10

        dao.deleteById(linkId) shouldBe true
        dao.findById(linkId) shouldBe null
    }
}
