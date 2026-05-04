-- Уровни (основной элемент учебного процесса)
CREATE TABLE IF NOT EXISTS Levels (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор уровня',
    name VARCHAR(128) NOT NULL COMMENT 'Название уровня',
    block_id UUID NOT NULL COMMENT 'Ссылка на блок, к которому принадлежит уровень',
    position INT NOT NULL COMMENT 'Порядковый номер уровня внутри блока',
    tutorial_id UUID NULL COMMENT 'Ссылка на туториал',
    task_id UUID NOT NULL COMMENT 'Ссылка на задание',
    level_help_id UUID NULL COMMENT 'Ссылка на справочный материал уровня',
    required_in_block ENUM('Yes', 'No') NOT NULL DEFAULT 'No' COMMENT 'Индикатор обязательного уровня для прохождения в блоке',
    -- если удаляем блок в Blocks, то в таблице Levels удалятся все связанные записи
    FOREIGN KEY (block_id) REFERENCES Blocks(id) ON DELETE CASCADE,
    -- если удаляем туториал в Tutorials, то в таблице Levels все связанные записи остаются, но у них обнуляется tutorial_id
    FOREIGN KEY (tutorial_id) REFERENCES Tutorials(id) ON DELETE SET NULL,
    -- если удаляем задание в Tasks, то в таблице Levels удалятся все связанные записи
    FOREIGN KEY (task_id) REFERENCES Tasks(id) ON DELETE CASCADE,
    -- если удаляем справку уровня в LevelHelps, то в таблице Levels все связанные записи остаются, но у них обнуляется level_help_id
    FOREIGN KEY (level_help_id) REFERENCES LevelHelps(id) ON DELETE SET NULL,
    UNIQUE KEY uk_block_position (block_id, position) COMMENT 'Гарантирует уникальность позиции в пределах блока',
    UNIQUE KEY uk_block_name (block_id, name) COMMENT 'Гарантирует уникальность названия уровня в пределах блока'
) ENGINE=InnoDB COMMENT 'Уровни (основной элемент учебного процесса)';