-- Задания (что нужно сделать для прохождения уровня в целом)
CREATE TABLE IF NOT EXISTS Tasks (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор задания',
    description TEXT NOT NULL COMMENT 'Описание задания'
) ENGINE=InnoDB COMMENT 'Задания (что нужно сделать для прохождения уровня в целом)';