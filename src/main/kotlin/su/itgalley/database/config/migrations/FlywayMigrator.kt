package su.itgalley.database.config.migrations

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import su.itgalley.database.config.DbConfig
import java.io.File
import java.io.IOException
import java.sql.SQLException
import java.util.Properties
import kotlin.system.exitProcess

private const val EXIT_MISSING_CREDENTIALS = 2
private const val EXIT_FLYWAY_FAILURE = 3

fun main(args: Array<String>) {
    val (dbConfig, dbName) = loadConfig()
    val command = args.getOrNull(0)

    val flyway = createFlyway(dbConfig)
    executeCommand(flyway, command, dbName)
}

private fun loadConfig(): Pair<DbConfig, String> {
    val appProperties =
        Properties()
            .apply {
                val propertiesFile = File("app.properties")
                if (propertiesFile.exists()) {
                    load(propertiesFile.reader())
                }
            }

    val dbHost = appProperties.getProperty("db.host") ?: "localhost"
    val dbPort = appProperties.getProperty("db.port") ?: "3306"
    val dbName = appProperties.getProperty("db.base") ?: "db"

    // пользователь и пароль без дефолтных значений
    val dbUser: String? = appProperties.getProperty("db.user")
    val dbPassword: String? = appProperties.getProperty("db.password")

    if (dbUser == null || dbPassword == null) {
        println("ERROR: Missing database credentials")
        println("Add your password and username to file")
        exitProcess(EXIT_MISSING_CREDENTIALS)
    }

    val dbConfig =
        DbConfig(
            url = "jdbc:mariadb://$dbHost:$dbPort/$dbName",
            username = dbUser,
            password = dbPassword,
        )

    return dbConfig to dbName
}

private fun createFlyway(config: DbConfig): Flyway =
    Flyway.configure()
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
                // выводит количество применённых миграций
                println("Successfully applied ${result.migrationsExecuted} migrations")
            }
            "info" -> {
                val info = flyway.info()
                // выводит текущую миграцию в бд и количество миграций, которые ожидают применения
                println("Current version: ${info.current()?.version ?: "Empty"}")
                println("Pending: ${info.pending().size}")
            }
            "repair" -> {
                // выполняет синхронизацию
                flyway.repair()
                println("Schema history repaired (checksums aligned).")
            }
            "clean" -> {
                // полностью очищает базу данных
                print("DESTROY ALL DATA? Type 'YES' to confirm: ")
                if (readlnOrNull() == "YES") {
                    flyway.clean()
                    println("Database cleaned")
                } else {
                    println("Canceled")
                }
            }
            else -> printError()
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
