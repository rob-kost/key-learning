package su.itgalley.support

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
import su.itgalley.database.schema.KeyGroup
import su.itgalley.database.schema.RequiredInBlock
import su.itgalley.database.schema.SolutionType
import su.itgalley.dto.BlockDto
import su.itgalley.dto.CombinationDto
import su.itgalley.dto.CombinationKeyDto
import su.itgalley.dto.HotKeyDto
import su.itgalley.dto.KeysTableDto
import su.itgalley.dto.LevelDto
import su.itgalley.dto.LevelHelpDto
import su.itgalley.dto.SubTaskDto
import su.itgalley.dto.TaskDto
import su.itgalley.dto.TaskSubtaskDto
import su.itgalley.dto.TutorialDto
import java.util.UUID

object TestFixtures {
    fun createDaoRegistry(): DaoRegistry =
        DaoRegistry(
            keyDao = KeyDao(),
            blockDao = BlockDao(),
            tutorialDao = TutorialDao(),
            levelHelpDao = LevelHelpDao(),
            taskDao = TaskDao(),
            combinationDao = CombinationDao(),
            combinationKeyDao = CombinationKeyDao(),
            hotKeyDao = HotKeyDao(),
            subtaskDao = SubtaskDao(),
            taskSubtaskDao = TaskSubtaskDao(),
            levelDao = LevelDao(),
        )

    data class LevelGraph(
        val blockId: UUID,
        val levelId: UUID,
        val taskId: UUID,
        val tutorialId: UUID,
        val helpId: UUID,
        val typingSubtaskId: UUID,
        val hotkeySubtaskId: UUID,
        val hotKeyId: UUID,
        val combinationId: UUID,
        val controlKeyId: UUID,
        val letterKeyId: UUID,
    )

    fun seedLevelGraph(dao: DaoRegistry): LevelGraph {
        val blockId = UUID.randomUUID()
        val taskId = UUID.randomUUID()
        val tutorialId = UUID.randomUUID()
        val helpId = UUID.randomUUID()
        val levelId = UUID.randomUUID()
        val combinationId = UUID.randomUUID()
        val controlKeyId = UUID.randomUUID()
        val letterKeyId = UUID.randomUUID()
        val hotKeyId = UUID.randomUUID()
        val typingSubtaskId = UUID.randomUUID()
        val hotkeySubtaskId = UUID.randomUUID()

        dao.blockDao.save(BlockDto(blockId, "Basics", "Intro block"))
        dao.tutorialDao.save(TutorialDto(tutorialId, "Tutorial text"))
        dao.levelHelpDao.save(LevelHelpDto(helpId, "Help text"))
        dao.taskDao.save(TaskDto(taskId, "Practice task"))
        dao.combinationDao.save(CombinationDto(combinationId))
        dao.keyDao.save(KeysTableDto(controlKeyId, "Control", KeyGroup.CONTROLS))
        dao.keyDao.save(KeysTableDto(letterKeyId, "C", KeyGroup.SYMBOLS))
        dao.combinationKeyDao.save(
            CombinationKeyDto(UUID.randomUUID(), combinationId, controlKeyId, 1),
        )
        dao.combinationKeyDao.save(
            CombinationKeyDto(UUID.randomUUID(), combinationId, letterKeyId, 2),
        )
        dao.hotKeyDao.save(HotKeyDto(hotKeyId, blockId, "Control+C", combinationId))
        dao.subtaskDao.save(
            SubTaskDto(
                id = typingSubtaskId,
                description = "Type hello",
                solutionType = SolutionType.TYPING,
                stringSolution = "hello",
                keySolutionId = null,
            ),
        )
        dao.subtaskDao.save(
            SubTaskDto(
                id = hotkeySubtaskId,
                description = "Copy text",
                solutionType = SolutionType.HOTKEY,
                stringSolution = null,
                keySolutionId = hotKeyId,
            ),
        )
        dao.taskSubtaskDao.save(TaskSubtaskDto(UUID.randomUUID(), taskId, typingSubtaskId, 1))
        dao.taskSubtaskDao.save(TaskSubtaskDto(UUID.randomUUID(), taskId, hotkeySubtaskId, 2))
        dao.levelDao.save(
            LevelDto(
                id = levelId,
                name = "Level 1",
                blockId = blockId,
                position = 1,
                tutorialId = tutorialId,
                taskId = taskId,
                levelHelpId = helpId,
                requiredInBlock = RequiredInBlock.YES,
            ),
        )

        return LevelGraph(
            blockId = blockId,
            levelId = levelId,
            taskId = taskId,
            tutorialId = tutorialId,
            helpId = helpId,
            typingSubtaskId = typingSubtaskId,
            hotkeySubtaskId = hotkeySubtaskId,
            hotKeyId = hotKeyId,
            combinationId = combinationId,
            controlKeyId = controlKeyId,
            letterKeyId = letterKeyId,
        )
    }
}
