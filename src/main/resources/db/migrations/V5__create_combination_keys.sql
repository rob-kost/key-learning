-- Описание составляющих частей комбинаций: какие клавиши и в каком порядке
CREATE TABLE IF NOT EXISTS CombinationKeys (
    id UUID PRIMARY KEY DEFAULT uuid() COMMENT 'UUID записи о вхождении клавиши в комбинацию',
    combination_id UUID NOT NULL COMMENT 'Ссылка на комбинацию',
    key_id UUID NOT NULL COMMENT 'Ссылка на клавишу',
    position INT NOT NULL COMMENT 'Порядковый номер клавиши в комбинации',
    FOREIGN KEY (combination_id) REFERENCES Combinations(id) ON DELETE CASCADE,
    FOREIGN KEY (key_id) REFERENCES `Keys`(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_position (combination_id, `position`) COMMENT 'Уникальность позиции в пределах одной комбинации',
    UNIQUE KEY uk_combination_key (combination_id, key_id) COMMENT 'Запрет повторения одной и той же клавиши в комбинации'
) ENGINE=InnoDB COMMENT 'Описание составляющих частей комбинаций: какие клавиши и в каком порядке';