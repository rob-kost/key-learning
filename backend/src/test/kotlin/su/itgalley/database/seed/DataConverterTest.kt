package su.itgalley.database.seed

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType

class DataConverterTest {
    @Test
    fun `convertBlocksToSeedData creates block level and typing subtask`() {
        val blocks =
            listOf(
                InputBlock(
                    name = "Block One",
                    description = "First block",
                    levels =
                        listOf(
                            InputLevel(
                                name = "Level Alpha",
                                tutorial = "Tutorial text",
                                help = "Help text",
                                subtasks =
                                    listOf(
                                        InputSubtask(
                                            type = "TYPING",
                                            desc = "Type hello",
                                            solution = "hello",
                                        ),
                                    ),
                            ),
                        ),
                ),
            )

        val seedData = convertBlocksToSeedData(blocks)

        seedData.blocks shouldHaveSize 1
        seedData.blocks.single().let {
            it.ref shouldBe "block_one"
            it.name shouldBe "Block One"
            it.description shouldBe "First block"
        }

        seedData.levels shouldHaveSize 1
        seedData.levels.single().let { level ->
            level.name shouldBe "Level Alpha"
            level.blockRef shouldBe "block_one"
            level.position shouldBe 1
            level.requiredInBlock shouldBe RequiredInBlock.NO
            level.tutorialRef.shouldNotBeNull()
            level.levelHelpRef.shouldNotBeNull()
        }

        seedData.tutorials shouldHaveSize 1
        seedData.tutorials.single().content shouldBe "Tutorial text"

        seedData.levelHelps shouldHaveSize 1
        seedData.levelHelps.single().content shouldBe "Help text"

        seedData.subtasks shouldHaveSize 1
        seedData.subtasks.single().let { subtask ->
            subtask.description shouldBe "Type hello"
            subtask.solutionType shouldBe SolutionType.TYPING
            subtask.stringSolution shouldBe "hello"
            subtask.keySolutionRef.shouldBeNull()
        }

        seedData.taskSubtasks shouldHaveSize 1
        seedData.taskSubtasks.single().position shouldBe 1
    }

    @Test
    fun `convertBlocksToSeedData deduplicates shared hotkeys within block`() {
        val blocks =
            listOf(
                InputBlock(
                    name = "Shared",
                    description = "Block",
                    levels =
                        listOf(
                            InputLevel(
                                name = "Level 1",
                                tutorial = null,
                                help = null,
                                subtasks =
                                    listOf(
                                        InputSubtask(type = "HOTKEY", desc = "First", solution = "Control+C"),
                                        InputSubtask(type = "HOTKEY", desc = "Second", solution = "Control+C"),
                                    ),
                            ),
                        ),
                ),
            )

        val seedData = convertBlocksToSeedData(blocks)

        seedData.hotKeys shouldHaveSize 1
        seedData.combinations shouldHaveSize 1
        seedData.subtasks shouldHaveSize 2
        seedData.subtasks.map { it.keySolutionRef }.distinct() shouldHaveSize 1
    }

    @Test
    fun `convertBlocksToSeedData creates separate hotkeys for same combination in different blocks`() {
        val blocks =
            listOf(
                InputBlock(
                    name = "Block A",
                    description = "A",
                    levels =
                        listOf(
                            InputLevel(
                                name = "L1",
                                tutorial = null,
                                help = null,
                                subtasks =
                                    listOf(
                                        InputSubtask(type = "HOTKEY", desc = "Copy", solution = "Control+C"),
                                    ),
                            ),
                        ),
                ),
                InputBlock(
                    name = "Block B",
                    description = "B",
                    levels =
                        listOf(
                            InputLevel(
                                name = "L2",
                                tutorial = null,
                                help = null,
                                subtasks =
                                    listOf(
                                        InputSubtask(type = "HOTKEY", desc = "Copy", solution = "Control+C"),
                                    ),
                            ),
                        ),
                ),
            )

        val seedData = convertBlocksToSeedData(blocks)

        seedData.hotKeys shouldHaveSize 2
        seedData.combinations shouldHaveSize 1
    }

    @Test
    fun `convertBlocksToSeedData classifies function keys as controls`() {
        val blocks =
            listOf(
                InputBlock(
                    name = "Fn Keys",
                    description = "Block",
                    levels =
                        listOf(
                            InputLevel(
                                name = "F1",
                                tutorial = null,
                                help = null,
                                subtasks =
                                    listOf(
                                        InputSubtask(type = "HOTKEY", desc = "Help", solution = "F1"),
                                    ),
                            ),
                        ),
                ),
            )

        val seedData = convertBlocksToSeedData(blocks)

        seedData.keys.single().keyGroup shouldBe KeyGroup.CONTROLS
    }

    @Test
    fun `convertBlocksToSeedData throws on unknown subtask type`() {
        val blocks =
            listOf(
                InputBlock(
                    name = "Invalid",
                    description = "Block",
                    levels =
                        listOf(
                            InputLevel(
                                name = "Broken",
                                tutorial = null,
                                help = null,
                                subtasks =
                                    listOf(
                                        InputSubtask(type = "UNKNOWN", desc = "Bad", solution = "x"),
                                    ),
                            ),
                        ),
                ),
            )

        shouldThrow<IllegalStateException> {
            convertBlocksToSeedData(blocks)
        }
    }
}
