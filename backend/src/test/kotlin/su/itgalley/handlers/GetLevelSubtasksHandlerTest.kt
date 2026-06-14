package su.itgalley.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import su.itgalley.createRouter
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.LevelHelpDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.dao.TutorialDao
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType
import su.itgalley.dto.HotKeyDto
import su.itgalley.dto.KeyWithPositionDto
import su.itgalley.dto.LevelDto
import su.itgalley.dto.LevelHelpDto
import su.itgalley.dto.SubTaskDto
import su.itgalley.dto.TutorialDto
import java.util.UUID

class GetLevelSubtasksHandlerTest {
    private val levelDao = mockk<LevelDao>()
    private val subtaskDao = mockk<SubtaskDao>()
    private val hotKeyDao = mockk<HotKeyDao>()
    private val tutorialDao = mockk<TutorialDao>()
    private val levelHelpDao = mockk<LevelHelpDao>()

    private val handler =
        routes(
            "/api/levels/{levelId}" bind
                getLevelSubtasksHandler(levelDao, subtaskDao, hotKeyDao, tutorialDao, levelHelpDao),
        )

    private val mapper = jacksonObjectMapper()

    @Test
    fun `returns bad request for invalid level id`() {
        val response = handler(Request(Method.GET, "/api/levels/not-a-uuid"))

        response.status shouldBe Status.BAD_REQUEST
        response.bodyString() shouldBe "Invalid level ID format"
    }

    @Test
    fun `returns not found when level does not exist`() {
        val levelId = UUID.randomUUID()
        every { levelDao.findById(levelId) } returns null

        val response = handler(Request(Method.GET, "/api/levels/$levelId"))

        response.status shouldBe Status.NOT_FOUND
        response.bodyString() shouldBe "Level not found"
    }

    @Test
    fun `returns level subtasks with tutorial help and hotkey combination`() {
        val levelId = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val tutorialId = UUID.randomUUID()
        val helpId = UUID.randomUUID()
        val subtaskId = UUID.randomUUID()
        val hotKeyId = UUID.randomUUID()
        val combinationId = UUID.randomUUID()

        every { levelDao.findById(levelId) } returns
            LevelDto(
                id = levelId,
                name = "Hotkeys",
                blockId = blockId,
                position = 1,
                tutorialId = tutorialId,
                taskId = taskId,
                levelHelpId = helpId,
                requiredInBlock = RequiredInBlock.NO,
            )
        every { subtaskDao.getSubtasksByLevelWithPosition(levelId) } returns
            listOf(
                Pair(
                    SubTaskDto(
                        id = subtaskId,
                        description = "Copy text",
                        solutionType = SolutionType.HOTKEY,
                        stringSolution = null,
                        keySolutionId = hotKeyId,
                    ),
                    1,
                ),
            )
        every { hotKeyDao.findById(hotKeyId) } returns
            HotKeyDto(
                id = hotKeyId,
                blockId = blockId,
                description = "Control+C",
                keyCombinationId = combinationId,
            )
        every { hotKeyDao.getKeysForCombination(combinationId) } returns
            listOf(
                KeyWithPositionDto(key = "Control", position = 1),
                KeyWithPositionDto(key = "C", position = 2),
            )
        every { tutorialDao.findById(tutorialId) } returns TutorialDto(tutorialId, "Tutorial content")
        every { levelHelpDao.findById(helpId) } returns LevelHelpDto(helpId, "Help content")

        val response = handler(Request(Method.GET, "/api/levels/$levelId"))

        response.status shouldBe Status.OK

        val body: Map<String, Any?> = mapper.readValue(response.bodyString())
        body["tutorial"] shouldBe "Tutorial content"
        body["help"] shouldBe "Help content"

        @Suppress("UNCHECKED_CAST")
        val subtasks = body["subtasks"] as List<Map<String, Any?>>
        subtasks shouldHaveSize 1
        subtasks.single().let { subtask ->
            subtask["id"] shouldBe subtaskId.toString()
            subtask["solutionType"] shouldBe "HOTKEY"
            subtask["description"] shouldBe "Copy text"
            subtask["stringSolution"].shouldBeNull()
            @Suppress("UNCHECKED_CAST")
            val combination = subtask["combination"] as List<Map<String, String>>
            combination shouldHaveSize 2
            combination[0]["key"] shouldBe "Control"
            combination[1]["key"] shouldBe "C"
        }
    }

    @Test
    fun `returns bad request when level id path is missing`() {
        val handlerWithoutTemplate =
            routes(
                "/api/levels" bind
                    getLevelSubtasksHandler(levelDao, subtaskDao, hotKeyDao, tutorialDao, levelHelpDao),
            )

        val response = handlerWithoutTemplate(Request(Method.GET, "/api/levels"))

        response.status shouldBe Status.BAD_REQUEST
        response.bodyString() shouldBe "Missing level ID"
    }

    @Test
    fun `returns empty combination when hotkey solution has no linked hotkey`() {
        val levelId = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val subtaskId = UUID.randomUUID()

        every { levelDao.findById(levelId) } returns
            LevelDto(
                id = levelId,
                name = "Broken hotkey",
                blockId = blockId,
                position = 1,
                tutorialId = null,
                taskId = taskId,
                levelHelpId = null,
                requiredInBlock = RequiredInBlock.NO,
            )
        every { subtaskDao.getSubtasksByLevelWithPosition(levelId) } returns
            listOf(
                Pair(
                    SubTaskDto(
                        id = subtaskId,
                        description = "Missing hotkey",
                        solutionType = SolutionType.HOTKEY,
                        stringSolution = null,
                        keySolutionId = null,
                    ),
                    1,
                ),
            )

        val response = handler(Request(Method.GET, "/api/levels/$levelId"))

        response.status shouldBe Status.OK

        @Suppress("UNCHECKED_CAST")
        val subtasks = mapper.readValue<Map<String, Any?>>(response.bodyString())["subtasks"] as List<Map<String, Any?>>

        @Suppress("UNCHECKED_CAST")
        val combination = subtasks.single()["combination"] as List<Map<String, String>>
        combination shouldHaveSize 0
    }

    @Test
    fun `returns empty combination when hotkey record is not found`() {
        val levelId = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val subtaskId = UUID.randomUUID()
        val missingHotKeyId = UUID.randomUUID()

        every { levelDao.findById(levelId) } returns
            LevelDto(
                id = levelId,
                name = "Missing record",
                blockId = blockId,
                position = 1,
                tutorialId = null,
                taskId = taskId,
                levelHelpId = null,
                requiredInBlock = RequiredInBlock.NO,
            )
        every { subtaskDao.getSubtasksByLevelWithPosition(levelId) } returns
            listOf(
                Pair(
                    SubTaskDto(
                        id = subtaskId,
                        description = "Stale hotkey id",
                        solutionType = SolutionType.HOTKEY,
                        stringSolution = null,
                        keySolutionId = missingHotKeyId,
                    ),
                    1,
                ),
            )
        every { hotKeyDao.findById(missingHotKeyId) } returns null

        val response = handler(Request(Method.GET, "/api/levels/$levelId"))

        response.status shouldBe Status.OK

        @Suppress("UNCHECKED_CAST")
        val subtasks = mapper.readValue<Map<String, Any?>>(response.bodyString())["subtasks"] as List<Map<String, Any?>>

        @Suppress("UNCHECKED_CAST")
        val combination = subtasks.single()["combination"] as List<Map<String, String>>
        combination shouldHaveSize 0
    }

    @Test
    fun `returns typing subtask without combination keys`() {
        val levelId = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val subtaskId = UUID.randomUUID()

        every { levelDao.findById(levelId) } returns
            LevelDto(
                id = levelId,
                name = "Typing",
                blockId = blockId,
                position = 1,
                tutorialId = null,
                taskId = taskId,
                levelHelpId = null,
                requiredInBlock = RequiredInBlock.NO,
            )
        every { subtaskDao.getSubtasksByLevelWithPosition(levelId) } returns
            listOf(
                Pair(
                    SubTaskDto(
                        id = subtaskId,
                        description = "Type word",
                        solutionType = SolutionType.TYPING,
                        stringSolution = "hello",
                        keySolutionId = null,
                    ),
                    1,
                ),
            )

        val response = handler(Request(Method.GET, "/api/levels/$levelId"))

        response.status shouldBe Status.OK

        val body: Map<String, Any?> = mapper.readValue(response.bodyString())
        body["tutorial"].shouldBeNull()
        body["help"].shouldBeNull()

        @Suppress("UNCHECKED_CAST")
        val subtasks = body["subtasks"] as List<Map<String, Any?>>
        subtasks.single().let { subtask ->
            subtask["solutionType"] shouldBe "TYPING"
            subtask["stringSolution"] shouldBe "hello"
            @Suppress("UNCHECKED_CAST")
            val combination = subtask["combination"] as List<Map<String, String>>
            combination shouldHaveSize 0
        }
    }
}
