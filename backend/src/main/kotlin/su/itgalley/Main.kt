package su.itgalley

import kotlinx.serialization.json.Json
import org.http4k.server.Jetty
import org.http4k.server.asServer
import su.itgalley.database.config.DatabaseConfig
import su.itgalley.database.config.DbConfig
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.CombinationDao
import su.itgalley.database.dao.CombinationKeyDao
import su.itgalley.database.dao.DaoRegistry
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.KeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.LevelHelpDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.dao.TaskDao
import su.itgalley.database.dao.TaskSubtaskDao
import su.itgalley.database.dao.TutorialDao
import su.itgalley.database.seed.DatabaseSeeder
import su.itgalley.database.seed.InputBlock
import su.itgalley.database.seed.convertBlocksToSeedData
import java.io.File
import java.util.Properties

fun main() {
    val config = loadDatabaseConfig()
    DatabaseConfig.init(config, validateSchema = true)

    val daoRegistry = createDaoRegistry()
    convertAndGenerateSeedData()

    val seeder = DatabaseSeeder(daoRegistry)
    seeder.seed()

    startServer(daoRegistry)
}

private fun loadDatabaseConfig(): DbConfig {
    val properties =
        Properties().apply {
            val propFile = File("app.properties")
            if (!propFile.exists()) error("app.properties not found")
            load(propFile.reader())
        }

    val dbHost = properties.getProperty("db.host", "localhost")
    val dbPort = properties.getProperty("db.port", "3306")
    val dbName = properties.getProperty("db.base", "keyldb")
    val dbUser = properties.getProperty("db.user") ?: error("Missing db.user in app.properties")
    val dbPassword = properties.getProperty("db.password") ?: error("Missing db.password in app.properties")

    return DbConfig(
        url = "jdbc:mariadb://$dbHost:$dbPort/$dbName",
        username = dbUser,
        password = dbPassword,
        poolSize = 10,
    )
}

private fun createDaoRegistry(): DaoRegistry {
    val blockDao = BlockDao()
    val levelDao = LevelDao()
    val subtaskDao = SubtaskDao()
    val hotKeyDao = HotKeyDao()
    val keyDao = KeyDao()
    val tutorialDao = TutorialDao()
    val levelHelpDao = LevelHelpDao()
    val taskDao = TaskDao()
    val combinationDao = CombinationDao()
    val combinationKeyDao = CombinationKeyDao()
    val taskSubtaskDao = TaskSubtaskDao()

    return DaoRegistry(
        keyDao = keyDao,
        blockDao = blockDao,
        tutorialDao = tutorialDao,
        levelHelpDao = levelHelpDao,
        taskDao = taskDao,
        combinationDao = combinationDao,
        combinationKeyDao = combinationKeyDao,
        hotKeyDao = hotKeyDao,
        subtaskDao = subtaskDao,
        taskSubtaskDao = taskSubtaskDao,
        levelDao = levelDao,
    )
}

private fun convertAndGenerateSeedData() {
    val inputFile = File("src/main/resources/KeyLearning_content.json")
    val outputFile = File("src/main/resources/seed_data.json")

    // Проверяем, нужно ли обновлять seed_data.json
    if (outputFile.exists() && outputFile.lastModified() >= inputFile.lastModified()) {
        println("seed_data.json is already up to date, conversion not required")
        return
    }

    val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    val jsonString = inputFile.readText()
    val simpleJson = json.decodeFromString<List<InputBlock>>(jsonString)
    val seedData = convertBlocksToSeedData(simpleJson)

    outputFile.writeText(json.encodeToString(seedData))

    // Копируем в build, чтобы classloader нашёл
    val buildFile = File("build/resources/main/seed_data.json")
    buildFile.parentFile.mkdirs()
    buildFile.writeText(json.encodeToString(seedData))

    println("Conversion completed. seed_data.json file created")
}

private fun startServer(daoRegistry: DaoRegistry) {
    val blockDao = daoRegistry.blockDao
    val levelDao = daoRegistry.levelDao
    val subtaskDao = daoRegistry.subtaskDao
    val hotKeyDao = daoRegistry.hotKeyDao
    val tutorialDao = daoRegistry.tutorialDao
    val levelHelpDao = daoRegistry.levelHelpDao

    val app =
        createRouter(
            blockDao,
            levelDao,
            subtaskDao,
            hotKeyDao,
            tutorialDao,
            levelHelpDao,
        )
    val port = 8228
    val server = app.asServer(Jetty(port)).start()
    println("Server started on http://localhost:$port/")
    server.block()
}
