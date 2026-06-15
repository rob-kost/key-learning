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
    fun loadFromFileOrNull(): Properties? {
        val file = File("app.properties")
        if (!file.exists()) return null
        return Properties().apply { load(file.reader()) }
    }

    fun getValue(envVar: String, propertyKey: String, default: String? = null): String {
        return System.getenv(envVar)
            ?: loadFromFileOrNull()?.getProperty(propertyKey)
            ?: default
            ?: error("Either $envVar environment variable or $propertyKey in app.properties must be set")
    }

    val dbType = System.getenv("DB_TYPE") ?: "mariadb"   // по умолчанию MariaDB

    return if (dbType.equals("h2", ignoreCase = true)) {
        // H2 in-memory, не требует пароля
        DbConfig(
            url = "jdbc:h2:mem:keyldb;DB_CLOSE_DELAY=-1",
            username = "sa",
            password = "",
            poolSize = 10,
        )
    } else {
        val dbHost = getValue("DB_HOST", "db.host", "localhost")
        val dbPort = getValue("DB_PORT", "db.port", "3306")
        val dbName = getValue("DB_NAME", "db.base", "keyldb")
        val dbUser = getValue("DB_USER", "db.user")
        val dbPassword = getValue("DB_PASSWORD", "db.password")

        DbConfig(
            url = "jdbc:mariadb://$dbHost:$dbPort/$dbName",
            username = dbUser,
            password = dbPassword,
            poolSize = 10,
        )
    }
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
    val classLoader = ClassLoader.getSystemClassLoader()
    val inputStream =
        classLoader.getResourceAsStream("KeyLearningBlock1.json")
            ?: error("KeyLearningBlock1.json not found in classpath")

    val json =
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }

    val jsonString = inputStream.bufferedReader().use { it.readText() }
    val simpleJson = json.decodeFromString<List<InputBlock>>(jsonString)
    val seedData = convertBlocksToSeedData(simpleJson)

    val outputDir = File("src/main/resources/")
    outputDir.mkdirs()
    val outputFile = File(outputDir, "seed_data.json")
    outputFile.writeText(json.encodeToString(seedData))

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
