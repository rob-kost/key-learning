-- нельзя добавить подзадачу с hk из блока А в задачу, которая уже используется в уровне с блоком В

DELIMITER //

-- Проверяет соответствие блоков в Levels и HotKeys при создании новой записи в таблице TaskSubtasks
DROP TRIGGER IF EXISTS trg_check_tasksubtasks_block_insert;
CREATE TRIGGER trg_check_tasksubtasks_block_insert
BEFORE INSERT ON TaskSubtasks
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;

    -- Проверяем привязан ли уже этот task_id к какому-то уровню
    -- и отличается ли block_id уровня от block_id добавляемого хоткея
    SELECT COUNT(*)
    INTO conflict_count
    FROM Levels AS l
    JOIN Subtasks AS s ON s.id = NEW.subtask_id
    JOIN HotKeys AS hk ON s.key_solution_id = hk.id
    WHERE l.task_id = NEW.task_id
      AND l.block_id != hk.block_id;

    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ошибка (TaskSubtasks INSERT): HotKey этой подзадачи принадлежит другому блоку';
    END IF;
END; //

-- Проверяет соответствие блоков в Levels и HotKeys при изменении записей в таблице TaskSubtasks
DROP TRIGGER IF EXISTS trg_check_tasksubtasks_block_update;
CREATE TRIGGER trg_check_tasksubtasks_block_update
BEFORE UPDATE ON TaskSubtasks
FOR EACH ROW
BEGIN
    DECLARE conflict_count INT;

    -- Проверяем привязан ли уже этот task_id к какому-то уровню
    -- и отличается ли block_id уровня от block_id добавляемого хоткея
    SELECT COUNT(*)
    INTO conflict_count
    FROM Levels AS l
    JOIN Subtasks AS s ON s.id = NEW.subtask_id
    JOIN HotKeys AS hk ON s.key_solution_id = hk.id
    WHERE l.task_id = NEW.task_id
    AND l.block_id != hk.block_id;

    IF conflict_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ошибка (TaskSubtasks UPDATE): HotKey этой подзадачи принадлежит другому блоку';
    END IF;
END; //

DELIMITER ;