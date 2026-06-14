package su.itgalley

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import su.itgalley.createRouter
import su.itgalley.database.dao.BlockDao
import su.itgalley.database.dao.HotKeyDao
import su.itgalley.database.dao.LevelDao
import su.itgalley.database.dao.LevelHelpDao
import su.itgalley.database.dao.SubtaskDao
import su.itgalley.database.dao.TutorialDao

class RouterTest {
    private val blockDao = mockk<BlockDao>(relaxed = true)
    private val levelDao = mockk<LevelDao>(relaxed = true)
    private val subtaskDao = mockk<SubtaskDao>(relaxed = true)
    private val hotKeyDao = mockk<HotKeyDao>(relaxed = true)
    private val tutorialDao = mockk<TutorialDao>(relaxed = true)
    private val levelHelpDao = mockk<LevelHelpDao>(relaxed = true)

    private val router =
        createRouter(
            blockDao = blockDao,
            levelDao = levelDao,
            subtaskDao = subtaskDao,
            hotKeyDao = hotKeyDao,
            tutorialDao = tutorialDao,
            levelHelpDao = levelHelpDao,
        )

    @Test
    fun `root endpoint returns health message`() {
        val response = router(Request(Method.GET, "/"))

        response.status shouldBe Status.OK
        response.bodyString() shouldBe "api is running :P"
        response.header("Access-Control-Allow-Origin") shouldBe "http://localhost:3000"
    }

    @Test
    fun `options request returns cors headers`() {
        val response = router(Request(Method.OPTIONS, "/api/navigation"))

        response.status shouldBe Status.OK
        response.header("Access-Control-Allow-Origin") shouldBe "http://localhost:3000"
        response.header("Access-Control-Allow-Methods") shouldBe "GET, POST, PUT, DELETE, OPTIONS"
        response.header("Access-Control-Allow-Headers") shouldBe "Origin, Content-Type, Accept"
    }

    @Test
    fun `navigation endpoint adds cors header to response`() {
        every { blockDao.findAllSorted() } returns emptyList()

        val response = router(Request(Method.GET, "/api/navigation"))

        response.status shouldBe Status.OK
        response.header("Access-Control-Allow-Origin") shouldBe "http://localhost:3000"
    }
}
