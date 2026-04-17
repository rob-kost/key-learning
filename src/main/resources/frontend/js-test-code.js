// Ждём полной загрузки DOM, хотя для получения элемента это не критично,
// но хорошая практика.
document.addEventListener('DOMContentLoaded', function() {
    // Находим блок для вывода информации
    const keyDisplayElement = document.getElementById('lastKey');

    // Если элемент не найден (вдруг опечатка в ID), выходим с ошибкой в консоль
    if (!keyDisplayElement) {
        console.error('Элемент с id "lastKey" не найден!');
        return;
    }

    // Функция-обработчик нажатия клавиши
    function handleKeyDown(event) {
        // Предотвращаем стандартное поведение для некоторых сочетаний,
        // чтобы браузер не выполнял свои действия (например, Ctrl+S не сохраняло страницу)
        // В реальном проекте стоит быть аккуратнее с preventDefault.
        // Здесь мы блокируем только если есть модификаторы, кроме Alt (Alt часто открывает меню).
        if (event.ctrlKey || event.metaKey || event.shiftKey) {
            event.preventDefault();
        }

        // Получаем основную клавишу (не модификатор)
        let mainKey = event.key;

        // Если нажата только клавиша-модификатор (Ctrl, Shift, Alt, Meta), 
        // то не показываем её как отдельную комбинацию, а просто игнорируем обновление,
        // либо можно вывести название модификатора. Я предпочту показывать комбинацию 
        // только тогда, когда есть основная клавиша.
        const modifierKeys = ['Control', 'Shift', 'Alt', 'Meta'];
        if (modifierKeys.includes(mainKey)) {
            // Если это просто модификатор — можно ничего не делать или вывести его название.
            // Но чтобы не затирать предыдущую комбинацию, оставим как есть.
            return;
        }

        // Собираем строку с модификаторами
        const modifiers = [];

        if (event.ctrlKey) modifiers.push('Ctrl');
        if (event.shiftKey) modifiers.push('Shift');
        if (event.altKey) modifiers.push('Alt');
        if (event.metaKey) modifiers.push('Meta'); // В Windows это клавиша Win, в MacOS — Command

        // Формируем итоговую строку
        let combination = '';

        if (modifiers.length > 0) {
            combination = modifiers.join(' + ') + ' + ';
        }

        // Добавляем основную клавишу, красиво оформляем пробел, если это пробел
        let displayKey = mainKey;
        if (mainKey === ' ') {
            displayKey = 'Пробел';
        } else if (mainKey === 'Enter') {
            displayKey = 'Enter';
        } else if (mainKey === 'Backspace') {
            displayKey = 'Backspace';
        } else if (mainKey === 'Tab') {
            displayKey = 'Tab';
        } else if (mainKey === 'Escape') {
            displayKey = 'Escape';
        } else if (mainKey === 'ArrowUp') {
            displayKey = '↑';
        } else if (mainKey === 'ArrowDown') {
            displayKey = '↓';
        } else if (mainKey === 'ArrowLeft') {
            displayKey = '←';
        } else if (mainKey === 'ArrowRight') {
            displayKey = '→';
        }

        combination += displayKey;

        // Обновляем текст в блоке
        keyDisplayElement.textContent = combination;
    }

    // Вешаем слушатель события keydown на весь документ
    document.addEventListener('keydown', handleKeyDown);
});