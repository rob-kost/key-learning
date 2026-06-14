package su.itgalley.database.seed

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType

class SeedDataSerializationTest {
    private val json =
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }

    @Test
    fun `seed data round trips through json serialization`() {
        val original =
            SeedData(
                keys = listOf(KeySeed("Control", KeyGroup.CONTROLS)),
                blocks = listOf(BlockSeed("basics", "Basics", "Intro")),
                tutorials = listOf(TutorialSeed("tut_1", "Tutorial")),
                levelHelps = listOf(LevelHelpSeed("help_1", "Help")),
                tasks = listOf(TaskSeed("task_1", "Task")),
                combinations = listOf(CombinationSeed("comb_1")),
                combinationKeys =
                    listOf(
                        CombinationKeySeed("comb_1", "Control", 1),
                    ),
                hotKeys =
                    listOf(
                        HotKeySeed("hk_1", "basics", "Control+C", "comb_1"),
                    ),
                subtasks =
                    listOf(
                        SubtaskSeed(
                            ref = "sub_1",
                            description = "Type",
                            solutionType = SolutionType.TYPING,
                            stringSolution = "abc",
                        ),
                    ),
                taskSubtasks = listOf(TaskSubtaskSeed("task_1", "sub_1", 1)),
                levels =
                    listOf(
                        LevelSeed(
                            ref = "level_1",
                            name = "Level 1",
                            blockRef = "basics",
                            position = 1,
                            tutorialRef = "tut_1",
                            taskRef = "task_1",
                            levelHelpRef = "help_1",
                            requiredInBlock = RequiredInBlock.YES,
                        ),
                    ),
            )

        val restored = json.decodeFromString<SeedData>(json.encodeToString(original))

        restored.keys shouldHaveSize 1
        restored.blocks.single().name shouldBe "Basics"
        restored.levels.single().requiredInBlock shouldBe RequiredInBlock.YES
        restored.subtasks.single().solutionType shouldBe SolutionType.TYPING
    }

    @Test
    fun `input block json deserializes from resource format`() {
        val payload =
            """
            [
              {
                "name": "Sample",
                "description": "Desc",
                "levels": [
                  {
                    "name": "L1",
                    "tutorial": "Tut",
                    "help": null,
                    "subtasks": [
                      { "type": "TYPING", "desc": "Type", "solution": "ok" }
                    ]
                  }
                ]
              }
            ]
            """.trimIndent()

        val blocks = json.decodeFromString<List<InputBlock>>(payload)

        blocks.single().levels.single().subtasks.single().type shouldBe "TYPING"
    }
}
