package su.itgalley.handlers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import su.itgalley.createRouter
import su.itgalley.support.DatabaseTestBase
import su.itgalley.support.TestFixtures

class HandlerIntegrationTest : DatabaseTestBase() {
    private val registry = TestFixtures.createDaoRegistry()
    private val mapper = jacksonObjectMapper()

    @Test
    fun `navigation handler returns seeded blocks and levels from database`() {
        val graph = TestFixtures.seedLevelGraph(registry)
        val handler = getNavigationHandler(registry.blockDao, registry.levelDao)

        val response = handler(Request(Method.GET, "/api/navigation"))

        response.status shouldBe Status.OK

        val body: List<Map<String, Any>> = mapper.readValue(response.bodyString())
        body shouldHaveSize 1
        body.single()["id"] shouldBe graph.blockId.toString()
        @Suppress("UNCHECKED_CAST")
        val levels = body.single()["levels"] as List<Map<String, Any>>
        levels.single()["id"] shouldBe graph.levelId.toString()
    }

    @Test
    fun `level subtasks handler returns full payload from database`() {
        val graph = TestFixtures.seedLevelGraph(registry)
        val handler =
            routes(
                "/api/levels/{levelId}" bind
                    getLevelSubtasksHandler(
                        registry.levelDao,
                        registry.subtaskDao,
                        registry.hotKeyDao,
                        registry.tutorialDao,
                        registry.levelHelpDao,
                    ),
            )

        val response = handler(Request(Method.GET, "/api/levels/${graph.levelId}"))

        response.status shouldBe Status.OK

        val body: Map<String, Any?> = mapper.readValue(response.bodyString())
        body["tutorial"] shouldBe "Tutorial text"
        body["help"] shouldBe "Help text"

        @Suppress("UNCHECKED_CAST")
        val subtasks = body["subtasks"] as List<Map<String, Any?>>
        subtasks shouldHaveSize 2
        subtasks[1]["solutionType"] shouldBe "HOTKEY"
        @Suppress("UNCHECKED_CAST")
        val combination = subtasks[1]["combination"] as List<Map<String, String>>
        combination.map { it["key"] } shouldBe listOf("Control", "C")
    }

    @Test
    fun `router serves navigation and level endpoints with real dao layer`() {
        val graph = TestFixtures.seedLevelGraph(registry)
        val router =
            createRouter(
                blockDao = registry.blockDao,
                levelDao = registry.levelDao,
                subtaskDao = registry.subtaskDao,
                hotKeyDao = registry.hotKeyDao,
                tutorialDao = registry.tutorialDao,
                levelHelpDao = registry.levelHelpDao,
            )

        val navigation = router(Request(Method.GET, "/api/navigation"))
        navigation.status shouldBe Status.OK

        val level = router(Request(Method.GET, "/api/levels/${graph.levelId}"))
        level.status shouldBe Status.OK
        level.header("Access-Control-Allow-Origin") shouldBe "http://localhost:3000"
    }
}
