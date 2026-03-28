package su.itgalley

import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

fun configureDatabase() {
    val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1" // in-memory
    val user = "sa"
    val password = ""

    // Настройка Exposed
    Database.connect(url, user = user, password = password)

    // Запуск Flyway миграций
    Flyway.configure()
        .dataSource(url, user, password)
        .load()
        .migrate()
}
