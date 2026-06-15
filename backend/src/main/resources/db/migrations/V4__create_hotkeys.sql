-- Комбинации клавиш для выполнения определённого действия
CREATE TABLE IF NOT EXISTS HotKeys (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID действия',
    block_id UUID NOT NULL COMMENT 'Ссылка на блок',
    description TEXT NOT NULL COMMENT 'Описание действия',
    key_combination_id UUID NOT NULL COMMENT 'Идентификатор комбинации клавиш, для действия описанного в description',
    UNIQUE KEY uk_block_combination (block_id, key_combination_id) COMMENT 'Гарантирует уникальность комбинации в пределах блока',
    UNIQUE KEY uk_block_description (block_id, description) COMMENT 'Гарантирует уникальность описания действия в пределах блока',
    -- если удаляем блок в Blocks, то в таблице Hotkeys удалятся все связанные записи
    FOREIGN KEY (block_id) REFERENCES Blocks(id) ON DELETE CASCADE,
    -- если удаляем комбинацию в Combinations, то в таблице Hotkeys удалятся все связанные записи
    FOREIGN KEY (key_combination_id) REFERENCES Combinations(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT 'Комбинации клавиш для выполнения определённого действия';