document.addEventListener('DOMContentLoaded', () => {
    const tutorialIcon = document.getElementById('tutorialIcon');
    const tutorialModal = document.getElementById('tutorialModal');
    const display = document.getElementById('keyDisplay');
    const taskTextEl = document.getElementById('taskText');
    const NUM_NEXT_TASKS = 3;
    const nextContainer = document.getElementById('nextTasksContainer');
    const nextTaskElements = [];

    // Создаём плитки для следующих заданий
    for (let i = 0; i < NUM_NEXT_TASKS; i++) {
        const el = document.createElement('div');
        el.className = 'small-rectangle';
        nextContainer.appendChild(el);
        nextTaskElements.push(el);
    }


const restartBtn = document.getElementById('restartLevel');
let currentLevelId = null; // глобальная переменная для текущего уровня
let currentLevelName = '';

    let activeTutorialContent = null;
    let tasksQueue = [];
    let currentTaskIndex = 0;
    let blocksData = [];
window.blocksData = blocksData;
let completedLevels = JSON.parse(localStorage.getItem('completedLevels')) || [];
window.completedLevels = completedLevels;

document.getElementById('staticPage').addEventListener('click', (e) => {
    const blockLink = e.target.closest('[data-block-id]');
    if (!blockLink) return;
    const blockId = blockLink.getAttribute('data-block-id');
    const block = blocksData.find(b => b.id === blockId);
    if (block) showBlockPage(block);
});

window.saveProgress=function(levelId, subtaskIndex) {
    sessionStorage.setItem('progress', JSON.stringify({ levelId, subtaskIndex }));
}


window.loadProgress=function() {
    const raw = sessionStorage.getItem('progress');
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
}


window.clearProgress=function() {
    sessionStorage.removeItem('progress');
}


restartBtn.addEventListener('click', () => {
    if (!currentLevelId) return;
    // Очищаем прогресс текущего уровня
    clearProgress();
    // Сохраняем команду перезапуска этого уровня
    sessionStorage.setItem('restartLevel', currentLevelId);
    location.reload();
});

const skipBtn1 = document.getElementById('skipLevelBtn');
if(skipBtn1){
    skipBtn1.style.display = 'flex';

skipBtn1.addEventListener('click', () => {
    if (!currentLevelId) return;
    if (!completedLevels.includes(currentLevelId)) {
        completedLevels.push(currentLevelId);
        localStorage.setItem('completedLevels', JSON.stringify(completedLevels));
    }
    clearProgress();
    // Переход к следующему уровню (как в конце startLevelSequence)
    const block = blocksData.find(b => b.levels.some(l => l.id === currentLevelId));
    if (block) {
        const currentLevelIndex = block.levels.findIndex(l => l.id === currentLevelId);
        const nextLevel = block.levels[currentLevelIndex + 1];
        if (nextLevel) {
            hideStaticPage();
            startLevelSequence(nextLevel.id);
        } else {
            advanceToNextBlock();
        }
    }
});
}

window.renderHomePage=function() {
    const blockListHTML = window.blocksData.length
        ? window.blocksData.map((b, i) =>
            `<h3 data-block-id="${b.id}" style="cursor:pointer; color:#81b4e3;">${i+1}. ${b.name}</h3>`
        ).join('')
        : '<p style="color:#aaa;">Нет доступных блоков</p>';

    const firstBlock = window.blocksData[0] || null;

    const html = marked.parse(HOME_PRE_MD) +
        (firstBlock ? `<button id="startLearningBtn" class="block-start-btn">Начать обучение</button>` : '') +
        `<h2 style="margin-top:2rem;">Содержание:</h2>` +
        blockListHTML +
        marked.parse(HOME_POST_MD);

    showStaticPage('Главная страница', html);

    if (firstBlock) {
        const startBtn = document.getElementById('startLearningBtn');
        if (startBtn) {
            startBtn.addEventListener('click', () => {
                showBlockPage(firstBlock);
            });
        }
    }
}


window.getLevelName=function(levelId) {
    for (const block of blocksData) {
        const level = block.levels.find(l => l.id === levelId);
        if (level) return level.name;
    }
    return levelId; // fallback на ID, если имя не найдено
}

    let currentBlockIndex = 0;
window.currentBlockIndex = currentBlockIndex;
	let activeHelpContent = null;
    let textKeyDownHandler = null;
    let hkKeyDownHandler = null;
    let hkKeyUpHandler = null;

const HUMAN_TO_SYSTEM = {
    'Ctrl': 'Control',
    'Win': 'Meta',
    'Alt': 'Alt',
    'Shift': 'Shift',
    'Esc': 'Escape',
    'Enter': 'Enter',
    'Tab': 'Tab',
    'Backspace': 'Backspace',
    'Space': ' ',
    '↑': 'ArrowUp',
    '↓': 'ArrowDown',
    '←': 'ArrowLeft',
    '→': 'ArrowRight'
};

const SYSTEM_TO_HUMAN = {
    'Control': 'Ctrl',
    'Meta': 'Win',
    'Alt': 'Alt',
    'Shift': 'Shift',
    'Escape': 'Esc',
    'Enter': 'Enter',
    'Tab': 'Tab',
    'Backspace': 'Backspace',
    ' ': 'Space',
    'ArrowUp': '↑',
    'ArrowDown': '↓',
    'ArrowLeft': '←',
    'ArrowRight': '→'
};
window.HUMAN_TO_SYSTEM = HUMAN_TO_SYSTEM;
window.SYSTEM_TO_HUMAN = SYSTEM_TO_HUMAN;

const HOME_PRE_MD = `## Привет!

В этом приложении ты сможешь понять, как взаимодействовать с клавиатурой быстрее и проще. В этом нам помогут горячие клавиши - hotkeys по-английски.

Обучение разделено на блоки. Каждый блок содержит несколько уровней. Последний уровень в блоке - контрольный. На каждом уровне нужно будет что-то вводить или нажимать. Но об этом позже :)

В самом начале мы познакомимся с клавиатурой и её возможностями. После - выучим несколько простых сочетаний клавиш. Если ты готов - жми кнопку ниже!

Примечание: Видите правую панель, с иконками книжки и знака вопроса? Это не просто картинки. 
Книжка - туториал. При нажатии она повторяет обучение, которое вы уже просмотрели перед стартом уровня, если что-то забыли.
Знак вопроса - справка. Он содержит подсказки к прохождению уровня.`;

const HOME_POST_MD = `---
## В главных ролях:


- Самый сексуальный мужик в мире: Косточкин Сергей - ИВТ-31
- Злодей британец: Быстров Егор - ИВТ-32
- Так себе шутник: Грачёв Артём - ИВТ-31
- Пубертатная язва: Кондратьев Никита - ИВТ-31
- Какой-то мужик: Мясников Юрий - ИВТ-31
- Недопонятый гений: Шабурин Константин - ИВТ-31

---
Проект создан в рамках курса Программная инженерия.

Преподаватель: Федулов Д.Д.

Copyright IT-Galley & DeadLine Team, 2026`;

const UNTRACKED_KEYS = [
    'Win', 'Alt', 'Tab', 'Esc',
    'F1', 'F2', 'F3', 'F4', 'F5', 'F6', 'F7', 'F8', 'F9', 'F10', 'F11', 'F12'
];



window.displayKey=function(k) {
    if (k === 'Space' || k === ' ') return 'Пробел';
    return SYSTEM_TO_HUMAN[k] || k;
}


window.adjustFontSize=function(element) {
    const maxFontSize = 3;   // rem
    const minFontSize = 1;   // rem
    let fontSize = maxFontSize;
    element.style.fontSize = fontSize + 'rem';
    // Уменьшаем, пока контент не помещается или шрифт не стал минимальным
    while (element.scrollHeight > element.clientHeight && fontSize > minFontSize) {
        fontSize -= 0.1;
        element.style.fontSize = fontSize + 'rem';
    }
}

    if (!display || !taskTextEl) {
        console.error('Не найдены #keyDisplay или #taskText');
        return;
    }

    if (tutorialIcon) {
        tutorialIcon.addEventListener('click', () => {
            if (activeTutorialContent) {
                showTutorialModal(activeTutorialContent, null);
            } else {
                showNotification('Нет активного туториала');
            }
        });
    }

const helpIcon = document.getElementById('helpIcon');
if (helpIcon) {
    helpIcon.addEventListener('click', () => {
        if (activeHelpContent) {
            showTutorialModal(activeHelpContent, null, 'Справка');
        } else {
            showNotification('Нет справки для текущего уровня');
        }
    });
}

const themeToggle = document.getElementById('themeToggle');
if (themeToggle) {
    // При загрузке проверяем сохранённую тему
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'light') {
        document.body.classList.add('light-theme');
        themeToggle.textContent = '🌙';
    }
    themeToggle.addEventListener('click', () => {
        document.body.classList.toggle('light-theme');
        const isLight = document.body.classList.contains('light-theme');
        localStorage.setItem('theme', isLight ? 'light' : 'dark');
        themeToggle.textContent = isLight ? '🌙' : '☀️';
    });
}

window.getTaskDisplayDescription=function(task) {
    if (task.solutionType === 'HOTKEY') {
        if (task.description && task.description.trim() !== '') {
            return task.description;
        }
        const keys = task.combination.map(k => displayKey(k.key)).join(' + ');
        return 'Введите сочетание клавиш: ' + keys;
    } else if (task.solutionType === 'TYPING') {
        if (task.description && task.description.trim() !== '') {
            return task.description;
        }
        const target = task.stringSolution || task.description || '';
        return 'Введите текст: ' + target;
    }
    return task.description || '';
}


    // ========== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ==========

window.updateNextTasks=function() {
    for (let i = 0; i < nextTaskElements.length; i++) {
        const taskIndex = currentTaskIndex + 1 + i;
        if (taskIndex < tasksQueue.length) {
            nextTaskElements[i].textContent = getTaskDisplayDescription(tasksQueue[taskIndex]);
        } else {
            nextTaskElements[i].textContent = '';
        }
    }
}

window.showNotification=function(message) {
        const toast = document.createElement('div');
        toast.textContent = message;
        toast.style.position = 'fixed';
        toast.style.bottom = '20px';
        toast.style.right = '20px';
        toast.style.backgroundColor = '#f44336';
        toast.style.color = 'white';
        toast.style.padding = '12px 20px';
        toast.style.borderRadius = '8px';
        toast.style.boxShadow = '0 4px 12px rgba(0,0,0,0.3)';
        toast.style.zIndex = '9999';
        toast.style.transition = 'opacity 0.3s';
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }


window.showTutorialModal=function(content, onClose, title = 'Туториал') {
	const modalTitle = document.getElementById('modalTitle');
	const modalHeader = document.querySelector('.modal-header');
if (modalHeader) {
    modalHeader.classList.toggle('help-header', title === 'Справка');
}
    if (modalTitle) modalTitle.textContent = title;
        const pages = content.split('\n---\n').map(s => s.trim());
        let currentPage = 0;

        const modal = tutorialModal;
        const body = document.getElementById('tutorialBody');
        const prevBtn = document.getElementById('prevPageBtn');
        const nextBtn = document.getElementById('nextPageBtn');
        const finishBtn = document.getElementById('finishTutorialBtn');
        const skipBtn = document.getElementById('skipTutorialBtn');
        const indicator = document.getElementById('pageIndicator');

        modal.style.display = 'flex';

function renderPage() {
            body.innerHTML = marked.parse(pages[currentPage]);
            indicator.textContent = `${currentPage + 1} / ${pages.length}`;
            prevBtn.disabled = currentPage === 0;
            if (currentPage === pages.length - 1) {
                nextBtn.style.display = 'none';
                finishBtn.style.display = 'inline-block';
            } else {
                nextBtn.style.display = 'inline-block';
                finishBtn.style.display = 'none';
            }
        }

function closeModal() {
            modal.style.display = 'none';
            if (onClose) onClose();
        }

        prevBtn.onclick = () => {
            if (currentPage > 0) {
                currentPage--;
                renderPage();
            }
        };
        nextBtn.onclick = () => {
            if (currentPage < pages.length - 1) {
                currentPage++;
                renderPage();
            }
        };
        finishBtn.onclick = closeModal;
        skipBtn.onclick = closeModal;

        renderPage();
    }



window.showStaticPage=function(title, contentHTML) {
if (restartBtn) restartBtn.style.display = 'none';
    const main = document.querySelector('.main-content');
    if (main) main.classList.add('static-mode');
    const sp = document.getElementById('staticPage');
    if (sp) {
        sp.innerHTML = `<h2>${title}</h2>${contentHTML}`;
	sp.style.display = '';
    }
}


window.hideStaticPage=function() {
	if (restartBtn) restartBtn.style.display = 'none';
    const main = document.querySelector('.main-content');
    if (main) main.classList.remove('static-mode');
    const sp = document.getElementById('staticPage');
    if (sp) {sp.innerHTML = ''; sp.style.display = 'none'; }
    document.querySelectorAll('#keyDisplay, #taskText, #nextTasksContainer').forEach(el => {
        if (el) el.style.display = '';
    });
}


window.showBlockPage=function(block) {
    const main = document.querySelector('.main-content');
    if (main) main.classList.add('static-mode');
    const sp = document.getElementById('staticPage');
    if (!sp) return;
    const blockIndex = window.blocksData ? window.blocksData.indexOf(block) : -1;
const firstAvailable = blockIndex >= 0 ? isLevelAvailable(window.blocksData, blockIndex, 0, block) : true;
const btnHTML = firstAvailable
    ? `<button id="startBlockBtn" class="block-start-btn">Начать первый уровень</button>`
    : '<p style="color:#f44336;">Сначала пройдите предыдущий блок</p>';
sp.innerHTML = marked.parse(block.description) + btnHTML;
    setActiveBlock(block.id);
    openBlockMenu(block.id);
    const btn = document.getElementById('startBlockBtn');
    if (btn) {
        btn.addEventListener('click', () => {
            hideStaticPage();
            startLevelSequence(block.levels[0].id);
        }, { once: true });
    }
}

    // ========== ЗАГРУЗКА НАВИГАЦИИ ==========
    async function fetchNavigationData() {
    try {
        const response = await fetch('http://localhost:8228/api/navigation');
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        return await response.json();
    } catch (error) {
        console.error('Ошибка загрузки навигации:', error);
        showNotification('Не удалось загрузить меню');
        return [];
    }
}



window.openBlockMenu=function(blockId) {
    const item = document.querySelector(`.menu-item[data-block-id="${blockId}"]`);
    if (item && !item.classList.contains('open')) {
        item.classList.add('open');
        const toggle = item.querySelector('.toggle');
        if (toggle) toggle.textContent = '▼';
    }
}


window.setActiveBlock=function(blockId) {
    document.querySelectorAll('.menu-item[data-block-id]').forEach(el => el.classList.remove('active'));
    const active = document.querySelector(`.menu-item[data-block-id="${blockId}"]`);
    if (active) active.classList.add('active');
}

window.isLevelAvailable=function(blocks, blockIndex, levelIndex, block) {
    if (blockIndex === 0 && levelIndex === 0) return true;
    if (levelIndex > 0) {
        const prevLevelId = block.levels[levelIndex - 1].id;
        return window.completedLevels.includes(prevLevelId);
    }
    const prevBlock = blocks[blockIndex - 1];
    return prevBlock.levels.every(l => window.completedLevels.includes(l.id));
}

window.buildMenu=function(blocks) {
        const menuRoot = document.getElementById('menuRoot');
        if (!menuRoot) return;

        let html = `<li class="menu-item home-item">Главная страница</li>`;


        blocks.forEach((block, index) => {
            const isOpen = false;
            html += `
                <li class="menu-item ${isOpen ? 'open' : ''}" data-block-id="${block.id}" data-block-index="${index}">
                    <span class="toggle">${isOpen ? '▼' : '▶'}</span> ${block.name}
                    <ul class="submenu">
                        ${block.levels.map((level, idx) => {
    const available = isLevelAvailable(blocks, index, idx, block);
    return `<li data-level-id="${level.id}" data-level-name="${level.name}" 
                class="${available ? '' : 'disabled'}"
                style="${available ? '' : 'color: #666; cursor: not-allowed;'}">
                • ${level.name} ${idx === block.levels.length - 1 ? '⭐' : ''}
            </li>`;
}).join('')}
                    </ul>
                </li>
            `;
        });

        menuRoot.innerHTML = html;

        menuRoot.addEventListener('click', (e) => {
            const toggle = e.target.closest('.toggle');
            const menuItem = e.target.closest('.menu-item');
            const levelItem = e.target.closest('[data-level-id]');

            // Клик по уровню
if (levelItem && !toggle) {
	if (levelItem.classList.contains('disabled')) {
        showNotification('Этот уровень пока недоступен');
        return;
    }
    const levelId = levelItem.getAttribute('data-level-id');
    const parentBlock = levelItem.closest('.menu-item[data-block-id]');
    if (parentBlock) {
        const blockId = parentBlock.getAttribute('data-block-id');
        setActiveBlock(blockId);
    }
    hideStaticPage();
    startLevelSequence(levelId);
	saveProgress(levelId, 0);
    return;
}

            // Сворачивание/разворачивание
            if (toggle && menuItem) {
                if (menuItem.classList.contains('open')) {
                    menuItem.classList.remove('open');
                    toggle.textContent = '▶';
                } else {
                    menuItem.classList.add('open');
                    toggle.textContent = '▼';
                }
                return;
            }

            // Клик по блоку
            if (menuItem && !menuItem.classList.contains('home-item') && menuItem.hasAttribute('data-block-id')) {
                const blockId = menuItem.getAttribute('data-block-id');
                const block = blocksData.find(b => b.id === blockId);
                if (block) showBlockPage(block);
                return;
            }

            // Главная страница
if (menuItem && menuItem.classList.contains('home-item')) {
    renderHomePage();
}
        });
    }

    fetchNavigationData()
    .then(blocks => {
        blocksData = blocks;
        buildMenu(blocks);
        const saved = loadProgress();
        if (saved && saved.levelId) {
            // Пытаемся восстановить уровень
            hideStaticPage();
            startLevelSequence(saved.levelId, saved.subtaskIndex || 0);
        } else {
            renderHomePage();
        }
	const restartLevel = sessionStorage.getItem('restartLevel');
if (restartLevel) {
    sessionStorage.removeItem('restartLevel');
    hideStaticPage();
    startLevelSequence(restartLevel, 0);
    return;
}
    })


    // ========== ЗАГРУЗКА ЗАДАНИЙ УРОВНЯ ==========
    async function fetchLevelTasks(levelId) {
    display.textContent = 'Загрузка...';
    display.style.color = '#aaa';

    try {
        const response = await fetch(`http://localhost:8228/api/levels/${levelId}`);
        if (!response.ok) throw new Error(`Ошибка: ${response.status}`);
        const levelData = await response.json();
        return levelData;
    } catch (error) {
        console.error('Ошибка загрузки уровня:', error);
        showNotification('Не удалось загрузить уровень');
        return { tutorial: null, subtasks: [] };
    }
}

window.advanceToNextBlock=function() {
    const nextIndex = window.currentBlockIndex + 1;
    if (nextIndex < window.blocksData.length) {
        const nextBlock = window.blocksData[nextIndex];
        showBlockPage(nextBlock);
    } else {
	clearProgress()
        showStaticPage('🎉 Поздравляем!', '<p>Вы прошли все блоки!</p>');
    }
}

    // ========== ЗАПУСК ПОСЛЕДОВАТЕЛЬНОСТИ БЛОКА ==========
    async function startLevelSequence(levelId, startSubtaskIndex = 0) {
	hideStaticPage();
	currentLevelId = levelId;
restartBtn.style.display = 'flex'; // показать кнопку
if (skipBtn1) skipBtn1.style.display = 'flex';
    const block = blocksData.find(b => b.levels.some(l => l.id === levelId));
    if (block) currentBlockIndex = blocksData.indexOf(block);

    display.textContent = 'Загрузка...';
    display.style.color = '#aaa';

    let levelData;
    try {
        levelData = await fetchLevelTasks(levelId);
    } catch (e) {
        console.error(e);
        showNotification('Ошибка загрузки уровня');
        return;
    }
activeHelpContent = levelData.help || null;

    const subtasks = levelData.subtasks || [];
    if (subtasks.length === 0) {
        showNotification('В этом уровне нет заданий');
        return;
    }

    tasksQueue = subtasks;
    currentTaskIndex = (startSubtaskIndex !== undefined) ? startSubtaskIndex : 0;
    updateNextTasks();

    // Установка туториала уровня
    activeTutorialContent = levelData.tutorial || null;

    const runSubtasks = async () => {
        while (currentTaskIndex < tasksQueue.length) {
            const task = tasksQueue[currentTaskIndex];

            await startLevel(task);
            currentTaskIndex++;
            updateNextTasks();
		saveProgress(levelId, currentTaskIndex);
        }

                // Все подзадачи уровня пройдены
	clearProgress();
	if (restartBtn) restartBtn.style.display = 'none';

if (skipBtn1) skipBtn1.style.display = 'none';	
	if (!completedLevels.includes(levelId)) {
    completedLevels.push(levelId);
    localStorage.setItem('completedLevels', JSON.stringify(completedLevels));
}
        const levelName = getLevelName(levelId);
	currentLevelName = levelName;
        display.textContent = `Уровень "${levelName}" пройден!`;
        adjustFontSize(display);
        display.style.color = '#4caf50';
        taskTextEl.textContent = '';
        nextTaskElements.forEach(el => el.textContent = '');

        // Через 2 секунды переходим к следующему уровню или блоку
        setTimeout(() => {
            const block = blocksData.find(b => b.levels.some(l => l.id === levelId));
            if (block) {
                const currentLevelIndex = block.levels.findIndex(l => l.id === levelId);
                const nextLevel = block.levels[currentLevelIndex + 1];
                if (nextLevel) {
                    // Запускаем следующий уровень этого же блока
			hideStaticPage(); 
                    startLevelSequence(nextLevel.id);
                } else {
                    // Все уровни блока пройдены — переходим к следующему блоку
                    advanceToNextBlock();
                }
            }
        }, 2000);
    };

    // Показываем туториал, если есть, потом запускаем подзадачи
    if (activeTutorialContent) {
        showTutorialModal(activeTutorialContent, () => runSubtasks());
    } else {
        runSubtasks();
    }
}


    // ========== ЗАПУСК ОДНОГО УРОВНЯ ==========
window.startLevel=function(taskData) {
	const prefix = currentLevelName ? `[${currentLevelName}] ` : '';
    if (taskData.solutionType === 'HOTKEY') {
        if (taskData.description && taskData.description.trim() !== '') {
            taskTextEl.textContent = taskData.description;
        } else {
            const keys = taskData.combination.map(k => displayKey(k.key)).join(' + ');
            taskTextEl.textContent = prefix + 'Введите сочетание клавиш: ' + keys;
        }
    } else if (taskData.solutionType === 'TYPING') {
        if (taskData.description && taskData.description.trim() !== '') {
            taskTextEl.textContent = prefix + taskData.description;
        } else {
            const target = taskData.stringSolution || taskData.description || '';
            taskTextEl.textContent = prefix + 'Введите текст: ' + target;
        }
    } else {
        taskTextEl.textContent = prefix + (taskData.description || '');
    }

    return new Promise((resolve) => {
        if (taskData.solutionType === 'TYPING') {
            startTextLevel(taskData).then(resolve);
        } else {
            startHKLevel(taskData).then(resolve);
        }
    });
}

window.getExpectedIdentifier=function(stepKey) {
    const key = stepKey;
    // Человеческое имя → системное
    if (HUMAN_TO_SYSTEM[key]) return HUMAN_TO_SYSTEM[key];
    // Если это системное имя (старые данные) – вернуть как есть
    if (key.length > 1 || ['Control','Shift','Alt','Meta'].includes(key)) return key;
    // Печатные символы → коды физических клавиш
    const codeMap = {
        ' ': 'Space',
        'A':'KeyA','B':'KeyB','C':'KeyC','D':'KeyD','E':'KeyE','F':'KeyF','G':'KeyG',
        'H':'KeyH','I':'KeyI','J':'KeyJ','K':'KeyK','L':'KeyL','M':'KeyM','N':'KeyN',
        'O':'KeyO','P':'KeyP','Q':'KeyQ','R':'KeyR','S':'KeyS','T':'KeyT','U':'KeyU',
        'V':'KeyV','W':'KeyW','X':'KeyX','Y':'KeyY','Z':'KeyZ',
        '0':'Digit0','1':'Digit1','2':'Digit2','3':'Digit3','4':'Digit4',
        '5':'Digit5','6':'Digit6','7':'Digit7','8':'Digit8','9':'Digit9',
        '!':'Digit1','@':'Digit2','#':'Digit3','$':'Digit4','%':'Digit5',
        '^':'Digit6','&':'Digit7','*':'Digit8','(':'Digit9',')':'Digit0',
    };
    return codeMap[key] || key;
}

window.getKeyIdentifier=function(event) {
    const key = event.key;
    // Пробел и модификаторы/спецклавиши (длина > 1)
    if (key === ' ') return 'Space';
    if (key.length > 1 || ['Control','Shift','Alt','Meta'].includes(key)) return key;
    // Печатные символы – используем физический код
    return event.code;
}
    // ---------- УРОВЕНЬ С КОМБИНАЦИЯМИ (HOTKEY) ----------
window.startHKLevel=function(taskData) {
        return new Promise((resolve) => {
            let lastErrorKey = null;
            display.style.color = '';

            const steps = [...taskData.combination];
	const sequentialMode = steps.some(step => UNTRACKED_KEYS.includes(step.key)) ||
    (steps.some(step => step.key === 'Ctrl') && steps.some(step => step.key === 'W')) ||
    (steps.some(step => step.key === 'Ctrl') && steps.some(step => step.key === 'T'));
	if (sequentialMode) {
    taskTextEl.textContent += ' (вводите клавиши по одной)';
}
            let currentStep = 0;
            let pressedKeys = new Set();
            let error = false;
            let finished = false;

function render() {
    if (finished) {
        // Показываем все шаги зелёными и галочку
        let html = '';
        for (let i = 0; i < steps.length; i++) {
            const step = steps[i];
            if (i > 0) html += '<span style="color:#aaa"> + </span>';
            html += `<span style="color:#4caf50; font-weight:bold;">${displayKey(step.key)}</span>`;
        }
        html += ' <span style="color:#4caf50; font-size:1.5rem;">✔</span>';
        display.innerHTML = html;
        adjustFontSize(display);
        return;
    }

                let html = '';
    for (let i = 0; i < currentStep; i++) {
        const step = steps[i];
        if (i > 0) html += '<span style="color:#aaa"> + </span>';
        html += `<span style="color:#4caf50; font-weight:bold;">${displayKey(step.key)}</span>`;
    }

    if (error && lastErrorKey) {
        if (currentStep > 0) html += '<span style="color:#aaa"> + </span>';
        html += `<span style="color:#f44336; font-weight:bold;">${displayKey(lastErrorKey)}</span>`;
    } else if (!error && currentStep > 0 && currentStep < steps.length) {
        html += '<span style="color:#aaa"> + </span>';
    }

    display.innerHTML = html;
    adjustFontSize(display);
}


window.arePreviousKeysPressed=function(stepIdx) {
                for (let i = 0; i < stepIdx; i++) {
                    const needed = getExpectedIdentifier(steps[i].key);
                    if (!pressedKeys.has(needed)) return false;
                }
                return true;
            }


window.hasExtraKeys=function(stepIdx) {
                const allowed = new Set();
                for (let i = 0; i < stepIdx; i++) {
                    allowed.add(getExpectedIdentifier(steps[i].key));
                }
                if (stepIdx < steps.length) {
                    const expected = steps[stepIdx];
                    allowed.add(getExpectedIdentifier(expected.key));
                }
                for (const k of pressedKeys) {
                    if (!allowed.has(k)) return true;
                }
                return false;
            }

function onKeyDown(event) {
                if (event.repeat || finished) return;

if (sequentialMode) {
    const id = getKeyIdentifier(event);
    const expected = steps[currentStep];
    const expectedId = getExpectedIdentifier(expected.key);

    // Отображаем нажатую клавишу (человеческое имя)
    let rawKey = event.key;
    if (rawKey === ' ') rawKey = ' ';
    lastErrorKey = SYSTEM_TO_HUMAN[rawKey] || rawKey;

    if (id === expectedId) {
        currentStep++;
        if (currentStep === steps.length) {
            finished = true;

            if (hkKeyDownHandler) document.removeEventListener('keydown', hkKeyDownHandler);
            if (hkKeyUpHandler) document.removeEventListener('keyup', hkKeyUpHandler);
            hkKeyDownHandler = null;
            hkKeyUpHandler = null;
            setTimeout(() => {
                activeTutorialContent = null;
                resolve();
            }, 1000);
        } else {
            lastErrorKey = null;
        }
    }
    // ошибка: lastErrorKey уже установлен
    render();
    return;
}

                let key = event.key;
if (key === ' ') key = ' ';
lastErrorKey = SYSTEM_TO_HUMAN[key] || key;   // запоминаем человеческое имя
                pressedKeys.add(getKeyIdentifier(event));

                if (!arePreviousKeysPressed(currentStep)) {
                    let rollbackTo = 0;
                    for (let i = 0; i < currentStep; i++) {
                        const needed = getExpectedIdentifier(steps[i].key);
                        if (!pressedKeys.has(needed)) {
                            rollbackTo = i;
                            break;
                        }
                    }
                    currentStep = rollbackTo;
                    error = hasExtraKeys(currentStep);
                    render();
                    return;
                }

                if (!error && hasExtraKeys(currentStep + 1)) {
                    error = true;
                    render();
                    return;
                }

                if (error) {
                    if (hasExtraKeys(currentStep + 1)) {
                        render();
                        return;
                    } else {
                        error = false;
                        lastErrorKey = null;
                    }
                }

                const expected = steps[currentStep];
const expectedId = getExpectedIdentifier(expected.key);

                if (getKeyIdentifier(event) === expectedId) {
                    currentStep++;
                    if (currentStep === steps.length) {
                        finished = true;
			render();
			if (hkKeyDownHandler) document.removeEventListener('keydown', hkKeyDownHandler);
                    if (hkKeyUpHandler) document.removeEventListener('keyup', hkKeyUpHandler);
                    hkKeyDownHandler = null;
                    hkKeyUpHandler = null;
                        setTimeout(() => {
                            activeTutorialContent = null;
                            resolve();
                        }, 1000);
                    } else {
                        render();
                    }
                } else {
                    error = true;
                    render();
                }
            }

function onKeyUp(event) {
                if (event.repeat || finished) return;
		if (sequentialMode) return;	

                const id = getKeyIdentifier(event);       // идентификатор отпущенной клавиши
    pressedKeys.delete(id);	

                if (pressedKeys.size === 0) {
                    currentStep = 0;
                    error = false;
                    lastErrorKey = null;
                    render();
                    return;
                }

                if (currentStep > 0) {
                    let rollbackNeeded = false;
                    let rollbackTo = 0;
                    for (let i = 0; i < currentStep; i++) {
			 const neededId = getExpectedIdentifier(steps[i].key);
                        if (id === neededId) {
                            rollbackNeeded = true;
                            rollbackTo = i;
                            break;
                        }
                    }
                    if (rollbackNeeded) {
                        currentStep = rollbackTo;
                        error = hasExtraKeys(currentStep);
                        if (!error) lastErrorKey = null;
                        render();
                        return;
                    }
                }

                if (error) {
                    if (arePreviousKeysPressed(currentStep) && !hasExtraKeys(currentStep)) {
                        error = false;
                        lastErrorKey = null;
                        render();
                    }
                }
            }





                        // Удаляем обработчики предыдущего HK-уровня, если они остались
            if (hkKeyDownHandler) document.removeEventListener('keydown', hkKeyDownHandler);
            if (hkKeyUpHandler) document.removeEventListener('keyup', hkKeyUpHandler);

            // Создаём и сохраняем новые обработчики
            hkKeyDownHandler = (e) => {
                preventSystemShortcuts(e);
                onKeyDown(e);
            };
            hkKeyUpHandler = onKeyUp;

            document.addEventListener('keydown', hkKeyDownHandler);
            document.addEventListener('keyup', hkKeyUpHandler);

            render();
        });
    }

window.preventSystemShortcuts=function(e) {
    const key = e.key;
    const ctrl = e.ctrlKey;
    const shift = e.shiftKey;
    const alt = e.altKey;
    const meta = e.metaKey;

    // Закрытие / перезагрузка
    if (
        (ctrl && key === 'w') ||
        (ctrl && shift && key === 'W') ||
        (alt && key === 'F4') ||
        key === 'F5' ||
        (ctrl && key === 'r')
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // Навигация (история)
    if (
        (alt && key === 'ArrowLeft') ||
        (alt && key === 'ArrowRight') ||
        key === 'Backspace' // отключаем переход назад
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // Вкладки и окна
    if (
        (ctrl && key === 't') ||
        (ctrl && key === 'n') ||
        (ctrl && shift && key === 'n') ||  
        (ctrl && key === 'Tab') ||         // переключение вкладок (мы не можем полностью заблокировать, но попытка есть)
        (ctrl && shift && key === 'Tab')
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // Инструменты разработчика / полный экран
    if (
        key === 'F11' ||
        key === 'F12'
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // Сохранение / печать / поиск
    if (
        (ctrl && key === 's') ||
        (ctrl && key === 'p') ||
        (ctrl && key === 'f') ||
        (ctrl && key === 'g')    // поиск далее
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // История / загрузки / закладки
    if (
        (ctrl && key === 'h') ||
        (ctrl && key === 'j') ||
        (ctrl && key === 'd')
    ) {
        e.preventDefault(); e.stopPropagation(); return;
    }

    // Блокировка всех Ctrl / Meta (как у вас уже было) лучше вынести в самый низ
    // чтобы не перекрыть специальные комбинации выше
    if (ctrl || meta) {
        e.preventDefault();
    }
}

    // ---------- УРОВЕНЬ С ТЕКСТОМ ----------
window.startTextLevel=function(taskData) {
    return new Promise((resolve) => {
         const targetText = taskData.stringSolution;
        let currentIndex = 0;
        let finished = false;
        let errorTimer = null;

        // Удаляем предыдущий обработчик, если был
        if (textKeyDownHandler) {
            document.removeEventListener('keydown', textKeyDownHandler);
            textKeyDownHandler = null;
        }

        // Функция обновления правильного текста
function updateDisplay() {
            if (finished) return;
            const typed = targetText.substring(0, currentIndex);
            display.textContent = typed;   // чёрный цвет задан ниже
		adjustFontSize(display);
        }

        // Показать ошибочный символ красным на 0.3 секунды
function showErrorChar(char) {
    const typed = targetText.substring(0, currentIndex);
    if (errorTimer) clearTimeout(errorTimer);
    const typedWithSpaces = typed.replace(/ /g, '&nbsp;');
    // Оборачиваем в div, чтобы flex‑контейнер не разбивал на отдельные элементы
    display.innerHTML = '<div style="text-align:center;width:100%">' +
        typedWithSpaces +
        `<span style="color:#f44336; font-weight:bold;">${char}</span>` +
        '</div>';
    errorTimer = setTimeout(() => {
        display.textContent = typed;   // возврат к обычному тексту
        errorTimer = null;
    }, 300);
}

function setDisplayColor(color) {
            display.style.color = color;
        }

function onKeyDown(event) {
            if (event.repeat || finished) return;

            const key = event.key;
            // Игнорируем служебные клавиши (Shift, Ctrl, Alt, Meta и др.) и длинные комбинации
            if (key.length > 1 && key !== ' ') return;

            // Дополнительно: если нажата клавиша с модификатором (например, Ctrl+C), 
            // то event.key может быть "c" при зажатом Ctrl, но само событие keydown 
            // для символа "c" пройдёт. Это нормально, так как пользователь хочет ввести "c".
            // Служебные клавиши сами по себе не проходят из-за длины.

            const expectedChar = targetText[currentIndex];

            if (key === expectedChar) {
                // Правильный символ
                if (errorTimer) {
                    clearTimeout(errorTimer);
                    errorTimer = null;
                }
                currentIndex++;
                setDisplayColor('black');
                updateDisplay();

                if (currentIndex === targetText.length) {
                   finished = true;
// Показываем набранный текст с зелёным цветом
setDisplayColor('#4caf50');
display.textContent = targetText;
adjustFontSize(display);
// Удаляем обработчик и завершаем через 1 сек
document.removeEventListener('keydown', onKeyDown);
textKeyDownHandler = null;
setTimeout(() => {
    activeTutorialContent = null;
    resolve();
}, 1000);
                }
            } else {
                // Неправильный символ
                showErrorChar(key);
            }
        }

        // Назначаем обработчик и сохраняем ссылку
        textKeyDownHandler = onKeyDown;
        document.addEventListener('keydown', onKeyDown);

        // Инициализация
        setDisplayColor('black');
        updateDisplay();
    });
}

});