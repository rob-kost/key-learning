package su.itgalley.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType
import java.util.UUID

class DtoTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `block dto supports copy and equality`() {
        val id = UUID.randomUUID()
        val dto = BlockDto(id, "Block", "Description")
        dto.copy(name = "Renamed") shouldBe BlockDto(id, "Renamed", "Description")
    }

    @Test
    fun `navigation dtos serialize to json`() {
        val blockId = UUID.randomUUID()
        val levelId = UUID.randomUUID()
        val dto =
            NavigationBlockDto(
                id = blockId,
                name = "Basics",
                description = "Intro",
                levels = listOf(NavigationLevelDto(levelId, "Level 1")),
            )

        val json = mapper.writeValueAsString(dto)
        json.contains(blockId.toString()) shouldBe true
        json.contains("Level 1") shouldBe true
    }

    @Test
    fun `level dto stores all references`() {
        val id = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val tutorialId = UUID.randomUUID()
        val helpId = UUID.randomUUID()

        val dto =
            LevelDto(
                id = id,
                name = "Level",
                blockId = blockId,
                position = 2,
                tutorialId = tutorialId,
                taskId = taskId,
                levelHelpId = helpId,
                requiredInBlock = RequiredInBlock.YES,
            )

        dto.requiredInBlock shouldBe RequiredInBlock.YES
        dto.position shouldBe 2
    }

    @Test
    fun `subtask dtos represent typing and hotkey solutions`() {
        val typing =
            SubTaskDto(
                id = UUID.randomUUID(),
                description = "Type",
                solutionType = SolutionType.TYPING,
                stringSolution = "abc",
                keySolutionId = null,
            )
        val hotkeyId = UUID.randomUUID()
        val hotkey =
            SubTaskDto(
                id = UUID.randomUUID(),
                description = "Copy",
                solutionType = SolutionType.HOTKEY,
                stringSolution = null,
                keySolutionId = hotkeyId,
            )

        typing.stringSolution shouldBe "abc"
        hotkey.keySolutionId shouldBe hotkeyId
    }

    @Test
    fun `hotkey and key dtos store related ids`() {
        val combinationId = UUID.randomUUID()
        val blockId = UUID.randomUUID()
        val hotKey =
            HotKeyDto(
                id = UUID.randomUUID(),
                blockId = blockId,
                description = "Control+C",
                keyCombinationId = combinationId,
            )
        val key = KeysTableDto(UUID.randomUUID(), "Control", KeyGroup.CONTROLS)
        val keyWithPosition = KeyWithPositionDto("Control", 1)

        hotKey.keyCombinationId shouldBe combinationId
        key.keyGroup shouldBe KeyGroup.CONTROLS
        keyWithPosition.position shouldBe 1
    }

    @Test
    fun `task and link dtos store references`() {
        val task = TaskDto(UUID.randomUUID(), "Description")
        val link =
            TaskSubtaskDto(
                id = UUID.randomUUID(),
                taskId = UUID.randomUUID(),
                subtaskId = UUID.randomUUID(),
                position = 3,
            )

        task.description shouldBe "Description"
        link.position shouldBe 3
    }

    @Test
    fun `response dto stores combination keys`() {
        val dto =
            SubtaskResponseDto(
                id = UUID.randomUUID(),
                solutionType = SolutionType.HOTKEY,
                description = "Copy",
                combination = listOf(mapOf("key" to "Control"), mapOf("key" to "C")),
                stringSolution = null,
            )

        dto.combination shouldBe listOf(mapOf("key" to "Control"), mapOf("key" to "C"))
    }

    @Test
    fun `tutorial and help dtos store content`() {
        val tutorial = TutorialDto(UUID.randomUUID(), "Tutorial text")
        val help = LevelHelpDto(UUID.randomUUID(), "Help text")
        tutorial.content shouldBe "Tutorial text"
        help.content shouldBe "Help text"
    }

    @Test
    fun `combination dtos store identifiers`() {
        val combination = CombinationDto(UUID.randomUUID())
        val combinationKey =
            CombinationKeyDto(
                id = UUID.randomUUID(),
                combinationId = UUID.randomUUID(),
                keyId = UUID.randomUUID(),
                position = 1,
            )

        combination.id shouldBe combination.id
        combinationKey.position shouldBe 1
    }
}
