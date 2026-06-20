package su.itgalley.database.config.migrations

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import su.itgalley.database.config.DbConfig
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.Properties
import kotlin.system.exitProcess

private const val EXIT_FLYWAY_FAILURE = 3

fun main(args: Array<String>) {
    val (dbConfig, dbName) = loadConfig()
    val command = args.getOrNull(0)

    val flyway = createFlyway(dbConfig)
    executeCommand(flyway, command, dbName)
}

private fun loadConfig(): Pair<DbConfig, String> {
    fun loadFromFileOrNull(): Properties? {
        val file = File("app.properties")
        if (!file.exists()) return null
        return Properties().apply { load(file.reader()) }
    }

    fun getValue(
        envVar: String,
        propertyKey: String,
        default: String? = null,
    ): String {
        return System.getenv(envVar)
            ?: loadFromFileOrNull()?.getProperty(propertyKey)
            ?: default
            ?: error("Either $envVar environment variable or $propertyKey in app.properties must be set")
    }

    val dbHost = getValue("DB_HOST", "db.host", "localhost")
    val dbPort = getValue("DB_PORT", "db.port", "3306")
    val dbName = getValue("DB_NAME", "db.base", "keyldb")
    val dbUser = getValue("DB_USER", "db.user") // обязательно
    val dbPassword = getValue("DB_PASSWORD", "db.password") // обязательно

    val dbConfig =
        DbConfig(
            url = "jdbc:mariadb://$dbHost:$dbPort/$dbName",
            username = dbUser,
            password = dbPassword,
        )
    return dbConfig to dbName
}

private fun createFlyway(config: DbConfig): Flyway =
    Flyway
        .configure()
        .dataSource(config.url, config.username, config.password)
        .locations("classpath:db/migrations")
        .baselineOnMigrate(true)
        .cleanDisabled(false)
        .load()

private fun printHelp() {
    println("Database Migration Tool")
    println("Usage: ./gradlew FlywayMigrator --args=[command]")
    println("Available commands: migrate, info, repair, clean")
}

private fun printError() {
    println("Incorrect command")
    println("Usage: ./gradlew FlywayMigrator --args=[command]")
    println("Available commands: migrate, info, repair, clean")
}

private fun executeCommand(
    flyway: Flyway,
    command: String?,
    dbName: String,
) {
    try {
        if (command.isNullOrBlank()) {
            printHelp()
            return
        }

        println("Running Flyway command: $command on $dbName")

        when (command) {
            "migrate" -> {
                val result = flyway.migrate()
                println("Successfully applied ${result.migrationsExecuted} migrations")
            }

            "info" -> {
                val info = flyway.info()
                println("Current version: ${info.current()?.version ?: "Empty"}")
                println("Pending: ${info.pending().size}")
            }

            "repair" -> {
                flyway.repair()
                println("Schema history repaired (checksums aligned)")
            }

            "clean" -> {
                print("DESTROY ALL DATA? Type 'YES' to confirm: ")
                if (readlnOrNull() == "YES") {
                    flyway.clean()
                    println("Database cleaned")
                } else {
                    println("Canceled")
                }
            }

            else -> {
                printError()
            }
        }
    } catch (e: FlywayException) {
        println("FATAL (Flyway): ${e.message}")
        exitProcess(EXIT_FLYWAY_FAILURE)
    } catch (e: SQLException) {
        println("FATAL (SQL): ${e.message}")
        exitProcess(EXIT_FLYWAY_FAILURE)
    } catch (e: IOException) {
        println("FATAL (IO): ${e.message}")
        exitProcess(EXIT_FLYWAY_FAILURE)
    }
}
