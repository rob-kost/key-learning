package su.itgalley.database.config

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DbConfigTest {
    @Test
    fun `db config stores connection settings with default pool size`() {
        val config =
            DbConfig(
                url = "jdbc:mariadb://localhost:3306/testdb",
                username = "user",
                password = "secret",
            )

        config.url shouldBe "jdbc:mariadb://localhost:3306/testdb"
        config.username shouldBe "user"
        config.password shouldBe "secret"
        config.poolSize shouldBe 20
    }

    @Test
    fun `db config accepts custom pool size`() {
        val config =
            DbConfig(
                url = "jdbc:mariadb://localhost:3306/testdb",
                username = "user",
                password = "secret",
                poolSize = 5,
            )

        config.poolSize shouldBe 5
    }
}
