-- Хранилище всех необходимых клавиш
CREATE TABLE IF NOT EXISTS `Keys` (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID клавиши',
    `key` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Название клавиши',
    key_group ENUM('CONTROLS', 'SYMBOLS') NOT NULL COMMENT 'К какой группе относится клавиша (CONTROLS или SYMBOLS)'
) ENGINE=InnoDB COMMENT 'Хранилище всех необходимых клавиш';