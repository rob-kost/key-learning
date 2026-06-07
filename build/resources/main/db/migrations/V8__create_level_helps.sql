-- Справочный материал уровней
CREATE TABLE IF NOT EXISTS LevelHelps (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор справки уровня',
    content TEXT NOT NULL COMMENT 'Текст справки уровня'
) ENGINE=InnoDB COMMENT 'Справочный материал уровней';