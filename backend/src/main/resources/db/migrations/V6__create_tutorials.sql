-- Туториалы (обучающий материал)
CREATE TABLE IF NOT EXISTS Tutorials (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'Уникальный идентификатор туториала',
    content TEXT NOT NULL COMMENT 'Текст туториала'
) ENGINE=InnoDB COMMENT 'Туториалы (обучающий материал)';