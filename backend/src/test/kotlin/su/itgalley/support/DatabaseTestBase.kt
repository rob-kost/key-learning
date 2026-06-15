package su.itgalley.support

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class DatabaseTestBase {
    @BeforeAll
    fun connectDatabase() {
        TestDatabase.init()
    }

    @BeforeEach
    fun resetDatabase() {
        TestDatabase.reset()
    }
}
