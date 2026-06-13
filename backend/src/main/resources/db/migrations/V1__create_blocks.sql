-- Блок горячих клавиш
CREATE TABLE IF NOT EXISTS Blocks (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID блока',
    name VARCHAR(128) NOT NULL UNIQUE COMMENT 'Название блока',
    description TEXT COMMENT 'Описание блока'
) ENGINE=InnoDB COMMENT 'Блок горячих клавиш';