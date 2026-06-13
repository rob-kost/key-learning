package su.itgalley

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
import java.io.File
import java.util.Properties

fun main() {
    // Загрузка конфигурации из app.properties
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

    val dbConfig =
        DbConfig(
            url = "jdbc:mariadb://$dbHost:$dbPort/$dbName",
            username = dbUser,
            password = dbPassword,
            poolSize = 10,
        )

    // Инициализация пула соединений HikariCP и подключение Exposed
    DatabaseConfig.init(dbConfig, validateSchema = true)

    // Создание экземпляров DAO для роутера
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

    // Регистрация всех DAO
    val daoRegistry =
        DaoRegistry(
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

    // Заполнение базы данными
    val seeder = DatabaseSeeder(daoRegistry)
    seeder.seed()

    // Создание роутера
    val app = createRouter(blockDao, levelDao, subtaskDao, hotKeyDao, tutorialDao, levelHelpDao)

    // Запуск сервера
    val port = 8228
    val server = app.asServer(Jetty(port)).start()
    println("Server started on http://localhost:$port/")
}
