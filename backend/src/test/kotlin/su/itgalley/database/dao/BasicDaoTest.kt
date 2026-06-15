package su.itgalley.database.dao

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import su.itgalley.support.DatabaseTestBase
import su.itgalley.support.TestFixtures
import java.util.UUID

class BlockDaoTest : DatabaseTestBase() {
    private val dao = BlockDao()

    @Test
    fun `save insert find update and delete block`() {
        val id = UUID.randomUUID()
        val block = dao.save(su.itgalley.dto.BlockDto(id, "Alpha", "First"))

        dao.findById(id).shouldNotBeNull().name shouldBe "Alpha"
        dao.findAll() shouldContainExactly listOf(block)
        dao.findAllSorted().single().name shouldBe "Alpha"
        dao.findByName("Alpha")?.id shouldBe id

        dao.save(su.itgalley.dto.BlockDto(id, "Beta", "Updated"))
        dao.findById(id)?.name shouldBe "Beta"

        dao.deleteById(id) shouldBe true
        dao.findById(id).shouldBeNull()
        dao.deleteById(id) shouldBe false
    }

    @Test
    fun `findAllSorted orders blocks by name`() {
        dao.save(su.itgalley.dto.BlockDto(UUID.randomUUID(), "Zulu", ""))
        dao.save(su.itgalley.dto.BlockDto(UUID.randomUUID(), "Alpha", ""))
        dao.save(su.itgalley.dto.BlockDto(UUID.randomUUID(), "Mike", ""))

        dao.findAllSorted().map { it.name } shouldContainExactly listOf("Alpha", "Mike", "Zulu")
    }

    @Test
    fun `findById returns null for missing block`() {
        dao.findById(UUID.randomUUID()).shouldBeNull()
    }

    @Test
    fun `maps null description to empty string`() {
        val id = UUID.randomUUID()
        dao.save(su.itgalley.dto.BlockDto(id, "NoDesc", ""))

        dao.findById(id)?.description shouldBe ""
    }
}

class KeyDaoTest : DatabaseTestBase() {
    private val dao = KeyDao()

    @Test
    fun `save insert find update and delete key`() {
        val id = UUID.randomUUID()
        val key =
            dao.save(
                su.itgalley.dto.KeysTableDto(id, "Control", su.itgalley.database.schema.KeyGroup.CONTROLS),
            )

        dao.findById(id) shouldBe key
        dao.findByKeyName("Control")?.id shouldBe id
        dao.findAll() shouldContainExactly listOf(key)

        dao.save(
            su.itgalley.dto.KeysTableDto(id, "Shift", su.itgalley.database.schema.KeyGroup.CONTROLS),
        )
        dao.findByKeyName("Shift")?.id shouldBe id

        dao.deleteById(id) shouldBe true
        dao.findById(id).shouldBeNull()
    }
}

class CombinationDaoTest : DatabaseTestBase() {
    private val dao = CombinationDao()

    @Test
    fun `save find and delete combination`() {
        val id = UUID.randomUUID()
        val combination = dao.save(su.itgalley.dto.CombinationDto(id))

        dao.findById(id) shouldBe combination
        dao.findAll() shouldContainExactly listOf(combination)
        dao.deleteById(id) shouldBe true
        dao.findById(id).shouldBeNull()
    }

    @Test
    fun `combination save ignores update for existing id`() {
        val id = UUID.randomUUID()
        dao.save(su.itgalley.dto.CombinationDto(id))
        dao.save(su.itgalley.dto.CombinationDto(id))

        dao.findAll() shouldHaveSize 1
    }
}

class TutorialDaoTest : DatabaseTestBase() {
    private val dao = TutorialDao()

    @Test
    fun `save find update and delete tutorial`() {
        val id = UUID.randomUUID()
        dao.save(su.itgalley.dto.TutorialDto(id, "Original"))

        dao.findById(id)?.content shouldBe "Original"
        dao.save(su.itgalley.dto.TutorialDto(id, "Updated"))
        dao.findById(id)?.content shouldBe "Updated"
        dao.deleteById(id) shouldBe true
    }
}

class LevelHelpDaoTest : DatabaseTestBase() {
    private val dao = LevelHelpDao()

    @Test
    fun `save find update and delete level help`() {
        val id = UUID.randomUUID()
        dao.save(su.itgalley.dto.LevelHelpDto(id, "Help"))

        dao.findById(id)?.content shouldBe "Help"
        dao.save(su.itgalley.dto.LevelHelpDto(id, "New help"))
        dao.findById(id)?.content shouldBe "New help"
        dao.deleteById(id) shouldBe true
    }
}

class TaskDaoTest : DatabaseTestBase() {
    private val dao = TaskDao()

    @Test
    fun `save find update and delete task`() {
        val id = UUID.randomUUID()
        dao.save(su.itgalley.dto.TaskDto(id, "Task description"))

        dao.findById(id)?.description shouldBe "Task description"
        dao.save(su.itgalley.dto.TaskDto(id, "Updated"))
        dao.findById(id)?.description shouldBe "Updated"
        dao.deleteById(id) shouldBe true
    }
}

class DaoRegistryTest : DatabaseTestBase() {
    @Test
    fun `registry exposes all dao instances`() {
        val registry = TestFixtures.createDaoRegistry()

        registry.blockDao.shouldNotBeNull()
        registry.levelDao.shouldNotBeNull()
        registry.subtaskDao.shouldNotBeNull()
        registry.hotKeyDao.shouldNotBeNull()
        registry.keyDao.shouldNotBeNull()
        registry.tutorialDao.shouldNotBeNull()
        registry.levelHelpDao.shouldNotBeNull()
        registry.taskDao.shouldNotBeNull()
        registry.combinationDao.shouldNotBeNull()
        registry.combinationKeyDao.shouldNotBeNull()
        registry.taskSubtaskDao.shouldNotBeNull()
    }
}
