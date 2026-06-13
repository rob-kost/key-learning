-- если подзадача уже находится в блоке А, ты не можешь поменять её решение на комбинацию из блока В

-- Проверяет соответствие блоков в Levels и HotKeys при изменении подзадачи
DROP TRIGGER IF EXISTS trg_check_subtasks_block_update;
CREATE TRIGGER trg_check_subtasks_block_update
BEFORE UPDATE ON Subtasks
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;
	-- Проверяем только если тип решения - горячая клавиша и она задана
    IF NEW.key_solution_id IS NOT NULL THEN

        SELECT COUNT(*)
        INTO conflict_count
        FROM TaskSubtasks AS ts
        JOIN Levels AS l ON ts.task_id = l.task_id
        JOIN HotKeys AS hk ON hk.id = NEW.key_solution_id
        WHERE ts.subtask_id = NEW.id
          AND l.block_id != hk.block_id;

        IF conflict_count > 0 THEN
                SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Ошибка (Subtasks UPDATE): Новый HotKey принадлежит другому блоку, а подзадача уже привязана к уровню';
        END IF;
    END IF;
END;
