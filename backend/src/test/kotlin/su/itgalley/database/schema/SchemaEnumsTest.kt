package su.itgalley.database.schema

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SchemaEnumsTest {
    @Test
    fun `key group enum contains expected values`() {
        KeyGroup.values().toList() shouldBe listOf(KeyGroup.CONTROLS, KeyGroup.SYMBOLS)
    }

    @Test
    fun `required in block enum contains expected values`() {
        RequiredInBlock.values().toList() shouldBe listOf(RequiredInBlock.YES, RequiredInBlock.NO)
    }

    @Test
    fun `solution type enum contains expected values`() {
        SolutionType.values().toList() shouldBe listOf(SolutionType.HOTKEY, SolutionType.TYPING)
    }
}
