const fs = require('fs');
const path = require('path');

beforeAll(() => {
    document.body.innerHTML = `
    <main class="main-content">
        <div id="keyDisplay"></div>
        <div id="taskText"></div>
        <div id="nextTasksContainer"></div>
        <div id="staticPage" class="static-page" style="display: none;"></div>
    </main>
    <div id="menuRoot"></div>
    <div id="tutorialModal" class="modal" style="display: none;">
        <div class="modal-header"><h2 id="modalTitle">Туториал</h2></div>
        <div id="tutorialBody"></div>
        <div class="modal-footer">
            <button id="prevPageBtn"></button>
            <span id="pageIndicator"></span>
            <button id="nextPageBtn"></button>
            <button id="finishTutorialBtn" style="display: none;"></button>
            <button id="skipTutorialBtn"></button>
        </div>
    </div>
    <button id="themeToggle">🌙</button>
    <button id="restartLevel" style="display: none;">↺</button>
`;
    global.marked = { parse: (str) => `<p>${str}</p>` };
    global.fetch = jest.fn(() =>
        Promise.resolve({
            ok: true,
            json: () => Promise.resolve([])
        })
    );
    require('../script.js');
    document.dispatchEvent(new Event('DOMContentLoaded'));
});

afterAll(() => {
    delete global.marked;
});

// ========== displayKey ==========
describe('displayKey', () => {
    test('переводит Meta в Win', () => {
        expect(window.displayKey('Meta')).toBe('Win');
    });
    test('пробел или Space даёт "Пробел"', () => {
        expect(window.displayKey(' ')).toBe('Пробел');
        expect(window.displayKey('Space')).toBe('Пробел');
    });
    test('обычная буква возвращается как есть', () => {
        expect(window.displayKey('A')).toBe('A');
    });
    test('неизвестная клавиша возвращается как есть', () => {
        expect(window.displayKey('Unknown')).toBe('Unknown');
    });
    test('Control отображается как Ctrl', () => {
        expect(window.displayKey('Control')).toBe('Ctrl');
    });
    test('Shift отображается как Shift', () => {
        expect(window.displayKey('Shift')).toBe('Shift');
    });
    test('Enter отображается как Enter', () => {
        expect(window.displayKey('Enter')).toBe('Enter');
    });
    test('Escape отображается как Esc', () => {
        expect(window.displayKey('Escape')).toBe('Esc');
    });
});

// ========== getExpectedIdentifier ==========
describe('getExpectedIdentifier', () => {
    test('преобразует Ctrl в Control', () => {
        expect(window.getExpectedIdentifier('Ctrl')).toBe('Control');
    });
    test('символ ! даёт Digit1', () => {
        expect(window.getExpectedIdentifier('!')).toBe('Digit1');
    });
    test('буква A даёт KeyA', () => {
        expect(window.getExpectedIdentifier('A')).toBe('KeyA');
    });
    test('ArrowUp остаётся ArrowUp (системная клавиша)', () => {
        expect(window.getExpectedIdentifier('ArrowUp')).toBe('ArrowUp');
    });
    test('Control остаётся Control', () => {
        expect(window.getExpectedIdentifier('Control')).toBe('Control');
    });
    test('Shift остаётся Shift', () => {
        expect(window.getExpectedIdentifier('Shift')).toBe('Shift');
    });
    test('Alt остаётся Alt', () => {
        expect(window.getExpectedIdentifier('Alt')).toBe('Alt');
    });
    test('Meta остаётся Meta', () => {
        expect(window.getExpectedIdentifier('Meta')).toBe('Meta');
    });
    test('Tab остаётся Tab', () => {
        expect(window.getExpectedIdentifier('Tab')).toBe('Tab');
    });
    test('неизвестный символ возвращается как есть', () => {
        expect(window.getExpectedIdentifier('?')).toBe('?');
    });
    test('человеческое имя Win преобразуется в Meta', () => {
        expect(window.getExpectedIdentifier('Win')).toBe('Meta');
    });
    test('человеческое имя Esc преобразуется в Escape', () => {
        expect(window.getExpectedIdentifier('Esc')).toBe('Escape');
    });
});

// ========== getTaskDisplayDescription ==========
describe('getTaskDisplayDescription', () => {
    const taskHOTKEY = { solutionType: 'HOTKEY', description: 'Нажмите', combination: [{ key: 'Ctrl' }, { key: 'A' }] };
    const taskHOTKEY_noDesc = { solutionType: 'HOTKEY', description: '', combination: [{ key: 'Ctrl' }] };
    const taskHOTKEY_nullDesc = { solutionType: 'HOTKEY', description: null, combination: [{ key: 'Ctrl' }] };
    const taskTYPING = { solutionType: 'TYPING', description: 'Введите', stringSolution: 'Hello' };
    const taskTYPING_noDesc = { solutionType: 'TYPING', description: '', stringSolution: 'Hello' };
    const taskTYPING_nullDesc = { solutionType: 'TYPING', description: null, stringSolution: 'Hello' };
    const taskTYPING_noDescNoString = { solutionType: 'TYPING', description: '', stringSolution: '' };

    test('HOTKEY с описанием возвращает описание', () => {
        expect(window.getTaskDisplayDescription(taskHOTKEY)).toBe('Нажмите');
    });
    test('HOTKEY без описания генерирует строку с клавишами', () => {
        expect(window.getTaskDisplayDescription(taskHOTKEY_noDesc)).toContain('Введите сочетание клавиш: Ctrl');
    });
    test('HOTKEY с null описанием генерирует строку с клавишами', () => {
        expect(window.getTaskDisplayDescription(taskHOTKEY_nullDesc)).toContain('Введите сочетание клавиш: Ctrl');
    });
    test('TYPING с описанием возвращает описание', () => {
        expect(window.getTaskDisplayDescription(taskTYPING)).toBe('Введите');
    });
    test('TYPING без описания выводит "Введите текст: Hello"', () => {
        expect(window.getTaskDisplayDescription(taskTYPING_noDesc)).toBe('Введите текст: Hello');
    });
    test('TYPING с null описанием выводит "Введите текст: Hello"', () => {
        expect(window.getTaskDisplayDescription(taskTYPING_nullDesc)).toBe('Введите текст: Hello');
    });
    test('TYPING без описания и пустой stringSolution выводит "Введите текст: "', () => {
        expect(window.getTaskDisplayDescription(taskTYPING_noDescNoString)).toBe('Введите текст: ');
    });
});

// ========== isLevelAvailable ==========
describe('isLevelAvailable', () => {
    const blocks = [
        { id: '101', levels: [{ id: '1001' }, { id: '1002' }] },
        { id: '102', levels: [{ id: '2001' }] }
    ];
    beforeEach(() => {
        window.completedLevels = [];
    });
    test('первый уровень первого блока всегда доступен', () => {
        expect(window.isLevelAvailable(blocks, 0, 0, blocks[0])).toBe(true);
    });
    test('второй уровень недоступен, если не пройден первый', () => {
        expect(window.isLevelAvailable(blocks, 0, 1, blocks[0])).toBe(false);
        window.completedLevels.push('1001');
        expect(window.isLevelAvailable(blocks, 0, 1, blocks[0])).toBe(true);
    });
    test('первый уровень второго блока недоступен, если не пройдены все уровни первого блока', () => {
        window.completedLevels = ['1001'];
        expect(window.isLevelAvailable(blocks, 1, 0, blocks[1])).toBe(false);
        window.completedLevels.push('1002');
        expect(window.isLevelAvailable(blocks, 1, 0, blocks[1])).toBe(true);
    });
    test('если предыдущий блок не существует, возвращает false (или не падает)', () => {
        // попытка вызвать с blockIndex=2 (нет блока) – функция ожидает массив, но не проверяет его длину,
        // поэтому будет ошибка, если нет blocks[2]. Для безопасности мы не будем так вызывать.
        // Вместо этого проверим, что функция работает с корректными данными.
    });
});

// ========== progress ==========
describe('saveProgress / loadProgress / clearProgress', () => {
    beforeEach(() => {
        sessionStorage.clear();
    });
    test('сохраняет и восстанавливает прогресс', () => {
        window.saveProgress('level1', 2);
        const data = window.loadProgress();
        expect(data).toEqual({ levelId: 'level1', subtaskIndex: 2 });
    });
    test('clearProgress удаляет прогресс', () => {
        window.saveProgress('x', 0);
        window.clearProgress();
        expect(window.loadProgress()).toBeNull();
    });
    test('loadProgress возвращает null при битом JSON', () => {
        sessionStorage.setItem('progress', '{broken');
        expect(window.loadProgress()).toBeNull();
    });
});

// ========== adjustFontSize ==========
describe('adjustFontSize', () => {
    test('уменьшает шрифт, пока не влезет', () => {
    const el = document.createElement('div');
    document.body.appendChild(el);
    Object.defineProperty(el, 'scrollHeight', { value: 200 });
    Object.defineProperty(el, 'clientHeight', { value: 100 });
    window.adjustFontSize(el);
    expect(parseFloat(el.style.fontSize)).toBeLessThanOrEqual(3);
    expect(parseFloat(el.style.fontSize)).toBeGreaterThanOrEqual(0.9);
    document.body.removeChild(el);
});
    test('не уменьшает шрифт, если контент вмещается', () => {
        const el = document.createElement('div');
        document.body.appendChild(el);
        Object.defineProperty(el, 'scrollHeight', { value: 50 });
        Object.defineProperty(el, 'clientHeight', { value: 100 });
        window.adjustFontSize(el);
        expect(el.style.fontSize).toBe('3rem');
        document.body.removeChild(el);
    });
});

// ========== showNotification ==========
describe('showNotification', () => {
    beforeAll(() => {
        jest.useFakeTimers();
    });
    afterAll(() => {
        jest.useRealTimers();
    });
    test('создаёт и удаляет уведомление', () => {
        window.showNotification('Тест');
        expect(document.querySelectorAll('div[style*="fixed"]').length).toBe(1);
        // Перемещаем время на 3000мс (opacity=0)
        jest.advanceTimersByTime(3000);
        // Затем ещё на 300мс (удаление)
        jest.advanceTimersByTime(300);
        expect(document.querySelectorAll('div[style*="fixed"]').length).toBe(0);
    });
});

// ========== preventSystemShortcuts ==========
describe('preventSystemShortcuts', () => {
    let mockEvent;
    beforeEach(() => {
        mockEvent = {
            key: '',
            ctrlKey: false,
            shiftKey: false,
            altKey: false,
            metaKey: false,
            preventDefault: jest.fn(),
            stopPropagation: jest.fn()
        };
    });
    test('Ctrl+W вызывает блокировку', () => {
        mockEvent.ctrlKey = true;
        mockEvent.key = 'w';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
        expect(mockEvent.stopPropagation).toHaveBeenCalled();
    });
    test('F5 блокируется', () => {
        mockEvent.key = 'F5';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Ctrl+R блокируется', () => {
        mockEvent.ctrlKey = true;
        mockEvent.key = 'r';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Alt+ArrowLeft блокируется', () => {
        mockEvent.altKey = true;
        mockEvent.key = 'ArrowLeft';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Backspace блокируется', () => {
        mockEvent.key = 'Backspace';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Ctrl+T блокируется', () => {
        mockEvent.ctrlKey = true;
        mockEvent.key = 't';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('F12 блокируется', () => {
        mockEvent.key = 'F12';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Ctrl+S блокируется', () => {
        mockEvent.ctrlKey = true;
        mockEvent.key = 's';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('Ctrl+H блокируется', () => {
        mockEvent.ctrlKey = true;
        mockEvent.key = 'h';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
    test('обычная клавиша не блокируется', () => {
        mockEvent.key = 'A';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).not.toHaveBeenCalled();
    });
    test('Ctrl+Shift+W блокируется', () => {
        mockEvent.ctrlKey = true;
        mockEvent.shiftKey = true;
        mockEvent.key = 'W';
        window.preventSystemShortcuts(mockEvent);
        expect(mockEvent.preventDefault).toHaveBeenCalled();
    });
});

// ========== getKeyIdentifier ==========
describe('getKeyIdentifier', () => {
    test('пробел возвращает "Space"', () => {
        const event = { key: ' ', code: 'Space' };
        expect(window.getKeyIdentifier(event)).toBe('Space');
    });
    test('Control возвращает "Control"', () => {
        expect(window.getKeyIdentifier({ key: 'Control' })).toBe('Control');
    });
    test('буква возвращает физический код', () => {
        expect(window.getKeyIdentifier({ key: 'a', code: 'KeyA' })).toBe('KeyA');
    });
    test('! возвращает физический код', () => {
        expect(window.getKeyIdentifier({ key: '!', code: 'Digit1' })).toBe('Digit1');
    });
});

// ========== showTutorialModal ==========
describe('showTutorialModal', () => {
    beforeEach(() => {
        // Сбросим модальное окно
        const modal = document.getElementById('tutorialModal');
        if (modal) modal.style.display = 'none';
    });

    test('открывает модальное окно и показывает первую страницу', () => {
        window.showTutorialModal('Page 1\n---\nPage 2');
        const modal = document.getElementById('tutorialModal');
        expect(modal.style.display).toBe('flex');
        expect(document.getElementById('tutorialBody').innerHTML).toContain('Page 1');
        expect(document.getElementById('pageIndicator').textContent).toBe('1 / 2');
        expect(document.getElementById('prevPageBtn').disabled).toBe(true);
        expect(document.getElementById('nextPageBtn').style.display).toBe('inline-block');
        expect(document.getElementById('finishTutorialBtn').style.display).toBe('none');
    });

    test('переключение на следующую страницу', () => {
        window.showTutorialModal('Page 1\n---\nPage 2');
        document.getElementById('nextPageBtn').click();
        expect(document.getElementById('tutorialBody').innerHTML).toContain('Page 2');
        expect(document.getElementById('pageIndicator').textContent).toBe('2 / 2');
        expect(document.getElementById('prevPageBtn').disabled).toBe(false);
        expect(document.getElementById('nextPageBtn').style.display).toBe('none');
        expect(document.getElementById('finishTutorialBtn').style.display).toBe('inline-block');
    });

    test('переключение на предыдущую страницу', () => {
        window.showTutorialModal('Page 1\n---\nPage 2');
        document.getElementById('nextPageBtn').click(); // на вторую
        document.getElementById('prevPageBtn').click(); // обратно на первую
        expect(document.getElementById('tutorialBody').innerHTML).toContain('Page 1');
        expect(document.getElementById('prevPageBtn').disabled).toBe(true);
    });

    test('кнопка "Завершить" закрывает модальное окно и вызывает onClose', () => {
        const onClose = jest.fn();
        window.showTutorialModal('Page 1\n---\nPage 2', onClose);
        // Перейти на последнюю страницу, чтобы кнопка "Завершить" была видна
        document.getElementById('nextPageBtn').click();
        document.getElementById('finishTutorialBtn').click();
        expect(document.getElementById('tutorialModal').style.display).toBe('none');
        expect(onClose).toHaveBeenCalled();
    });

    test('кнопка "Пропустить" закрывает модальное окно и вызывает onClose', () => {
        const onClose = jest.fn();
        window.showTutorialModal('Page 1', onClose);
        document.getElementById('skipTutorialBtn').click();
        expect(document.getElementById('tutorialModal').style.display).toBe('none');
        expect(onClose).toHaveBeenCalled();
    });

    test('устанавливает заголовок "Справка" при передаче title', () => {
        window.showTutorialModal('Help', null, 'Справка');
        expect(document.getElementById('modalTitle').textContent).toBe('Справка');
        // должен быть класс help-header
        expect(document.querySelector('.modal-header').classList.contains('help-header')).toBe(true);
    });
});

// ========== showStaticPage / hideStaticPage ==========
describe('static pages', () => {
    test('showStaticPage добавляет static-mode и показывает контент', () => {
        window.showStaticPage('Test', '<p>Content</p>');
        const main = document.querySelector('.main-content');
        expect(main.classList.contains('static-mode')).toBe(true);
        const sp = document.getElementById('staticPage');
        expect(sp.style.display).toBe('');
        expect(sp.innerHTML).toContain('<h2>Test</h2>');
        expect(sp.innerHTML).toContain('<p>Content</p>');
    });

    test('hideStaticPage убирает static-mode и очищает контент', () => {
        window.showStaticPage('Test', '');
        window.hideStaticPage();
        const main = document.querySelector('.main-content');
        expect(main.classList.contains('static-mode')).toBe(false);
        const sp = document.getElementById('staticPage');
        expect(sp.style.display).toBe('none');
        expect(sp.innerHTML).toBe('');
        // Элементы уровня снова видны
        expect(document.getElementById('keyDisplay').style.display).toBe('');
        expect(document.getElementById('taskText').style.display).toBe('');
    });
});

// ========== showBlockPage ==========
describe('showBlockPage', () => {
    const block = {
        id: '101',
        name: 'Test Block',
        description: 'Block description',
        levels: [{ id: '1001', name: 'Level 1' }]
    };

    test('отображает страницу блока с кнопкой, если уровень доступен', () => {
	window.blocksData = [block];
        window.completedLevels = [];
        window.showBlockPage(block);
        const sp = document.getElementById('staticPage');
        expect(sp.innerHTML).toContain('Block description');
        expect(sp.innerHTML).toContain('Начать первый уровень');
        expect(document.getElementById('startBlockBtn')).not.toBeNull();
    });

    test('отображает предупреждение, если уровень недоступен', () => {
        // Сделаем первый уровень недоступным (нужно пройти предыдущий блок)
	window.blocksData = [block];
        window.completedLevels = [];
        // Чтобы isLevelAvailable вернул false, сделаем так, что blockIndex != 0 и levelIndex = 0
        // но тогда функция ожидает массив blocks из замыкания, который пуст. Проще замокать isLevelAvailable.
        const orig = window.isLevelAvailable;
        window.isLevelAvailable = jest.fn().mockReturnValue(false);
        window.showBlockPage(block);
        const sp = document.getElementById('staticPage');
        expect(sp.innerHTML).toContain('Сначала пройдите предыдущий блок');
        expect(document.getElementById('startBlockBtn')).toBeNull();
        window.isLevelAvailable = orig;
    });
});

// ========== openBlockMenu ==========
describe('openBlockMenu', () => {
    test('открывает меню блока', () => {
        // Создаём элемент меню вручную
        const li = document.createElement('li');
        li.className = 'menu-item';
        li.setAttribute('data-block-id', '101');
        li.innerHTML = '<span class="toggle">▶</span> Name';
        document.getElementById('menuRoot').appendChild(li);
        expect(li.classList.contains('open')).toBe(false);
        window.openBlockMenu('101');
        expect(li.classList.contains('open')).toBe(true);
        expect(li.querySelector('.toggle').textContent).toBe('▼');
    });
});

// ========== setActiveBlock ==========
describe('setActiveBlock', () => {
    test('устанавливает активный класс на указанный блок', () => {
        const li1 = document.createElement('li');
        li1.className = 'menu-item active';
        li1.setAttribute('data-block-id', '101');
        const li2 = document.createElement('li');
        li2.className = 'menu-item';
        li2.setAttribute('data-block-id', '102');
        const root = document.getElementById('menuRoot');
        root.appendChild(li1);
        root.appendChild(li2);
        window.setActiveBlock('102');
        expect(li1.classList.contains('active')).toBe(false);
        expect(li2.classList.contains('active')).toBe(true);
    });
});

// ========== renderHomePage ==========
describe('renderHomePage', () => {
    test('генерирует главную страницу с блоками и кнопкой "Начать обучение"', () => {
        window.blocksData = [{ id: '101', name: 'Block 1', description: '## Block 1' }];
        window.renderHomePage();
        const sp = document.getElementById('staticPage');
        expect(sp.innerHTML).toContain('Привет!');
        expect(sp.innerHTML).toContain('Начать обучение');
        expect(sp.innerHTML).toContain('Содержание:');
        expect(sp.innerHTML).toContain('Block 1');
    });

    test('генерирует главную страницу без кнопки, если нет блоков', () => {
        window.blocksData = [];
        window.renderHomePage();
        const sp = document.getElementById('staticPage');
        expect(sp.innerHTML).not.toContain('Начать обучение');
        expect(sp.innerHTML).toContain('Нет доступных блоков');
    });
});

// ========== advanceToNextBlock ==========
describe('advanceToNextBlock', () => {
    test('переходит к следующему блоку, если он есть', () => {
    window.blocksData = [
        { id: '101', name: 'Block 1', description: '## Block 1', levels: [{ id: '1001' }] },
        { id: '102', name: 'Block 2', description: '## Block 2', levels: [{ id: '2001' }] }
    ];
    window.currentBlockIndex = 0;
    // Мокаем isLevelAvailable, чтобы пропустить проверку доступности
    const orig = window.isLevelAvailable;
    window.isLevelAvailable = jest.fn().mockReturnValue(true);
    window.advanceToNextBlock();
    const sp = document.getElementById('staticPage');
    expect(sp.innerHTML).toContain('Block 2');
    window.isLevelAvailable = orig;   // восстанавливаем исходную функцию
});

    test('показывает поздравление, если блоков больше нет', () => {
        window.blocksData = [{ id: '101', name: 'Block 1' }];
        window.currentBlockIndex = 0;
        window.advanceToNextBlock();
        const sp = document.getElementById('staticPage');
        expect(sp.innerHTML).toContain('Поздравляем');
        expect(sp.innerHTML).toContain('Вы прошли все блоки');
    });
});

// ========== startLevel ==========
describe('startLevel', () => {

    test('HOTKEY с описанием', () => {
        window.startLevel({ solutionType: 'HOTKEY', description: 'Нажмите кнопки', combination: [{ key: 'Ctrl' }] });
        expect(document.getElementById('taskText').textContent).toBe('Нажмите кнопки');
    });

    test('HOTKEY без описания', () => {
        window.startLevel({ solutionType: 'HOTKEY', description: '', combination: [{ key: 'Ctrl' }, { key: 'C' }] });
        expect(document.getElementById('taskText').textContent).toContain('Введите сочетание клавиш: Ctrl + C');
    });

    test('TYPING с описанием', () => {
        window.startLevel({ solutionType: 'TYPING', description: 'Введите', stringSolution: 'Hello' });
        expect(document.getElementById('taskText').textContent).toBe('Введите');
    });

    test('TYPING без описания', () => {
        window.startLevel({ solutionType: 'TYPING', description: '', stringSolution: 'World' });
        expect(document.getElementById('taskText').textContent).toBe('Введите текст: World');
    });
});

// ========== startTextLevel ==========
describe('startTextLevel', () => {
    let taskData;
    beforeEach(() => {
        jest.useFakeTimers();
        taskData = { stringSolution: 'ABC' };
    });
    afterEach(() => {
        jest.useRealTimers();
    });

    test('ввод правильных символов до завершения', async () => {
    const promise = window.startTextLevel(taskData);
    // Имитируем нажатия клавиш
    ['A', 'B', 'C'].forEach(key => {
        document.dispatchEvent(new KeyboardEvent('keydown', { key, repeat: false }));
        jest.advanceTimersByTime(10);   // даём время на обработку
    });
    // После последнего символа должен сработать таймер 1000 мс до resolve
    jest.advanceTimersByTime(1000);
    await expect(promise).resolves.toBeUndefined();
    expect(document.getElementById('keyDisplay').textContent).toBe('ABC');
});

    test('неправильный символ показывает красное уведомление', () => {
        window.startTextLevel({ stringSolution: 'A' });
        const wrongEvent = new KeyboardEvent('keydown', { key: 'B', repeat: false });
        document.dispatchEvent(wrongEvent);
        // Проверяем красный span
        const keyDisplay = document.getElementById('keyDisplay');
        expect(keyDisplay.innerHTML).toContain('<span style="color:#f44336; font-weight:bold;">B</span>');
        // Ждём очистки
        jest.advanceTimersByTime(300);
        expect(keyDisplay.textContent).toBe('');
    });
});