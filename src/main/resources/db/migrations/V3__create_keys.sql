-- Хранилище всех необходимых клавиш
CREATE TABLE IF NOT EXISTS `Keys` (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID клавиши',
    `key` VARCHAR(64) NOT NULL UNIQUE COMMENT 'Название клавиши',
    key_group ENUM('управляющие клавиши', 'печатные клавиши') NOT NULL COMMENT 'К какой группе относится клавиша (печатная или управляющая)'
) ENGINE=InnoDB COMMENT 'Хранилище всех необходимых клавиш';