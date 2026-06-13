-- Связь заданий с подзадачами
CREATE TABLE IF NOT EXISTS TaskSubtasks (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор записи о вхождении подзадачи в задачу',
    task_id UUID NOT NULL COMMENT 'Ссылка на задание',
    subtask_id UUID NOT NULL COMMENT 'Ссылка на подзадачу',
    position INT NOT NULL COMMENT 'Порядковый номер подзадачи в задании',
    -- если удаляем задачу в Tasks, то в таблице TaskSubtasks удалятся все связанные записи
    FOREIGN KEY (task_id) REFERENCES Tasks(id) ON DELETE CASCADE,
    -- если удаляем подзадачу в Subtasks, то в таблице TaskSubtasks удалятся все связанные записи
    FOREIGN KEY (subtask_id) REFERENCES Subtasks(id) ON DELETE CASCADE,
    UNIQUE KEY uk_task_position (task_id, position) COMMENT 'Гарантирует уникальность позиции в пределах задания'
) ENGINE=InnoDB COMMENT 'Связь заданий с подзадачами';