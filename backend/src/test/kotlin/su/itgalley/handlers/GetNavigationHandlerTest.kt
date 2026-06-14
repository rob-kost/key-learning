package su.itgalley.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import su.itgalley.createRouter
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.dto.BlockDto
import su.itgalley.dto.LevelDto
import java.util.UUID

class GetNavigationHandlerTest {
    private val blockDao = mockk<BlockDao>()
    private val levelDao = mockk<LevelDao>()
    private val handler = getNavigationHandler(blockDao, levelDao)
    private val mapper = jacksonObjectMapper()

    @Test
    fun `returns blocks with nested levels sorted by dao`() {
        val blockId = UUID.randomUUID()
        val levelId = UUID.randomUUID()
        val taskId = UUID.randomUUID()

        every { blockDao.findAllSorted() } returns
            listOf(
                BlockDto(id = blockId, name = "Basics", description = "Intro block"),
            )
        every { levelDao.findByBlockOrdered(blockId) } returns
            listOf(
                LevelDto(
                    id = levelId,
                    name = "Level 1",
                    blockId = blockId,
                    position = 1,
                    tutorialId = null,
                    taskId = taskId,
                    levelHelpId = null,
                    requiredInBlock = RequiredInBlock.NO,
                ),
            )

        val response = handler(Request(Method.GET, "/api/navigation"))

        response.status shouldBe Status.OK

        val body: List<Map<String, Any>> = mapper.readValue(response.bodyString())
        body shouldHaveSize 1
        body.single().let { block ->
            block["id"] shouldBe blockId.toString()
            block["name"] shouldBe "Basics"
            block["description"] shouldBe "Intro block"
            @Suppress("UNCHECKED_CAST")
            val levels = block["levels"] as List<Map<String, Any>>
            levels shouldHaveSize 1
            levels.single()["id"] shouldBe levelId.toString()
            levels.single()["name"] shouldBe "Level 1"
        }

        verify(exactly = 1) { blockDao.findAllSorted() }
        verify(exactly = 1) { levelDao.findByBlockOrdered(blockId) }
    }

    @Test
    fun `returns empty list when no blocks exist`() {
        every { blockDao.findAllSorted() } returns emptyList()

        val response = handler(Request(Method.GET, "/api/navigation"))

        response.status shouldBe Status.OK
        mapper.readValue<List<Map<String, Any>>>(response.bodyString()) shouldHaveSize 0
    }
}
