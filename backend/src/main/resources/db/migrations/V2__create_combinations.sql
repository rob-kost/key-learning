-- Записи комбинаций клавиш
CREATE TABLE IF NOT EXISTS Combinations (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID комбинации'
) ENGINE=InnoDB COMMENT 'Записи комбинаций клавиш';