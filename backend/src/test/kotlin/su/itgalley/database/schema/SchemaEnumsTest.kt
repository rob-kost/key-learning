package su.itgalley.database.schema

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SchemaEnumsTest {
    @Test
    fun `key group enum contains expected values`() {
        KeyGroup.entries shouldBe listOf(KeyGroup.CONTROLS, KeyGroup.SYMBOLS)
    }

    @Test
    fun `required in block enum contains expected values`() {
        RequiredInBlock.entries shouldBe listOf(RequiredInBlock.YES, RequiredInBlock.NO)
    }

    @Test
    fun `solution type enum contains expected values`() {
        SolutionType.entries shouldBe listOf(SolutionType.HOTKEY, SolutionType.TYPING)
    }
}
