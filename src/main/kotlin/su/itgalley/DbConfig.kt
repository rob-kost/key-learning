package su.itgalley

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import kotlin.system.exitProcess


data class DbConfig(
    val url: String,
    val username: String,
    val password: String,
    val poolSize: Int = 20
)


object DatabaseConfig {
    private lateinit var dataSource: HikariDataSource

    fun init(config: DbConfig, validateSchema: Boolean = true) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.username
            password = config.password
            driverClassName = "org.mariadb.jdbc.Driver"
            maximumPoolSize = config.poolSize
            connectionTimeout = 30000
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            addDataSourceProperty("cachePrepStmts", "true")
            addDataSourceProperty("prepStmtCacheSize", "250")
            addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        }

        dataSource = HikariDataSource(hikariConfig)

        Database.connect(dataSource)

        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:su/itgalley/db/migrations")
            .load()

        if (validateSchema) {
            validateDatabaseSchema(flyway)
        }
    }

    private fun validateDatabaseSchema(flyway: Flyway) {
        try {
            val migrationInfos = flyway.info().all()
            val numApplied = migrationInfos.count { it.state?.isApplied == true }
            val numPending = migrationInfos.count { it.state?.isApplied == false }

            println("Database Schema Validation:")
            println("Database: ${dataSource.jdbcUrl}")
            println("Applied migrations: $numApplied")
            println("Pending migrations: $numPending")
            println("Current version: ${flyway.info().current()?.version}")

            flyway.validate()

            println("Schema validation: PASSED")

            if (numPending > 0) {
                println("There are $numPending pending migrations!")
                println("Run FlywayMigrator to apply migrations before starting the application.")
            }

        } catch (e: Exception) {
            println("Database schema validation FAILED: ${e.message}")
            exitProcess(1)

        }
    }

    fun getDataSource() = dataSource

    fun close() = dataSource.close()
}