-- нельзя положить в уровень задачу, если внутри неё есть подзадачи с хоткеями из другого блока

DELIMITER //

-- Проверяет соответствие блоков в Levels и HotKeys при создании нового уровня
DROP TRIGGER IF EXISTS trg_check_levels_block_insert;
CREATE TRIGGER trg_check_levels_block_insert
BEFORE INSERT ON Levels
FOR EACH ROW
BEGIN
    DECLARE bad_hotkeys_count INT;

    -- Ищем хоткеи, привязанные к этой задаче, у которых block_id не совпадает с block_id уровня
    SELECT COUNT(*)
    INTO bad_hotkeys_count
    FROM TaskSubtasks AS ts
    JOIN Subtasks AS sub ON ts.subtask_id = sub.id
    JOIN HotKeys AS hk ON sub.key_solution_id = hk.id
    WHERE ts.task_id = NEW.task_id
      AND hk.block_id != NEW.block_id;

    -- Если нашли хоть один конфликт, то отменяем операцию и выдаем ошибку
    IF bad_hotkeys_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ошибка (Levels INSERT): Задача содержит подзадачи с HotKey из другого блока';
    END IF;
END; //

-- Проверяет соответствие блоков в Levels и HotKeys при изменении уровня
DROP TRIGGER IF EXISTS trg_check_levels_block_update;
CREATE TRIGGER trg_check_levels_block_update
BEFORE UPDATE ON Levels
FOR EACH ROW
BEGIN
    DECLARE bad_hotkeys_count INT;

    -- Ищем хоткеи, привязанные к этой задаче, у которых block_id не совпадает с block_id уровня
    SELECT COUNT(*)
    INTO bad_hotkeys_count
    FROM TaskSubtasks AS ts
    JOIN Subtasks AS sub ON ts.subtask_id = sub.id
    JOIN HotKeys AS hk ON sub.key_solution_id = hk.id
    WHERE ts.task_id = NEW.task_id
      AND hk.block_id != NEW.block_id;

    -- Если нашли хоть один конфликт, то отменяем операцию и выдаем ошибку
    IF bad_hotkeys_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Ошибка (Levels UPDATE): Задача содержит подзадачи с HotKey из другого блока';
    END IF;
END; //

DELIMITER ;