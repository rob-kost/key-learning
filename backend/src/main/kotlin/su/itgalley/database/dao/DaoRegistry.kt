package su.itgalley.database.dao

@Suppress("LongParameterList")
class DaoRegistry(
    val keyDao: KeyDao,
    val blockDao: BlockDao,
    val tutorialDao: TutorialDao,
    val levelHelpDao: LevelHelpDao,
    val taskDao: TaskDao,
    val combinationDao: CombinationDao,
    val combinationKeyDao: CombinationKeyDao,
    val hotKeyDao: HotKeyDao,
    val subtaskDao: SubtaskDao,
    val taskSubtaskDao: TaskSubtaskDao,
    val levelDao: LevelDao,
)
