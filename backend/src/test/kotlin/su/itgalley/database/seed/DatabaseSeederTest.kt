package su.itgalley.database.seed

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import su.itgalley.support.DatabaseTestBase
import su.itgalley.support.TestFixtures

class DatabaseSeederTest : DatabaseTestBase() {
    @Test
    fun `seed populates database from seed_data json`() {
        val registry = TestFixtures.createDaoRegistry()
        val seeder = DatabaseSeeder(registry)

        seeder.seed()

        registry.blockDao.findAll().isNotEmpty() shouldBe true
        registry.levelDao.findAll().isNotEmpty() shouldBe true
        registry.subtaskDao.findAll().isNotEmpty() shouldBe true
        registry.keyDao.findAll().isNotEmpty() shouldBe true
    }

    @Test
    fun `seed skips when database already contains blocks`() {
        val registry = TestFixtures.createDaoRegistry()
        registry.blockDao.save(
            su.itgalley.dto.BlockDto(
                java.util.UUID.randomUUID(),
                "Existing",
                "Already seeded",
            ),
        )

        val beforeCount = registry.levelDao.findAll().size
        DatabaseSeeder(registry).seed()
        val afterCount = registry.levelDao.findAll().size

        afterCount shouldBe beforeCount
        registry.blockDao.findAll() shouldHaveSize 1
    }
}
