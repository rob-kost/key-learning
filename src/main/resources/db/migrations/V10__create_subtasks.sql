-- Элементарные задания, из которых состоят задания
CREATE TABLE IF NOT EXISTS Subtasks (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор подзадачи',
    description TEXT NOT NULL COMMENT 'Описание подзадачи',
    solution_type ENUM('hotkey', 'typing') NOT NULL COMMENT 'Способ решения подзадания: ввод текста в редакторе или сочетание клавиш',
    string_solution TEXT NULL COMMENT 'Эталонная строка для типа "typing"',
    key_solution_id UUID NULL COMMENT 'Ссылка на комбинацию клавиш для типа "hotkey"',
    -- если удаляем комбинацию в HotKeys, то в таблице Subtasks удалятся все связанные записи
    FOREIGN KEY (key_solution_id) REFERENCES HotKeys(id) ON DELETE CASCADE,
    -- Для типа "typing" обязательно string_solution, для "hotkey" - key_solution_id
    CONSTRAINT chk_subtask_solution CHECK (
        (solution_type = 'typing' AND string_solution IS NOT NULL) OR
        (solution_type = 'hotkey' AND key_solution_id IS NOT NULL)
    ),
    -- Гарантирует, что для одного типа либо key_solution_id, либо string_solution будет NULL
    CONSTRAINT chk_subtask_clean_other_solution CHECK (
        (solution_type = 'typing' AND key_solution_id IS NULL) AND
        (solution_type = 'hotkey' AND string_solution IS NULL)
    )
) ENGINE=InnoDB COMMENT 'Элементарные задания, из которых состоят задания';