# Проект по программной инженерии «Key-Learning»

## Разработка

Для Frontend-разработки:

- VisualStudio Code

Backend:

- IntellijIDEA
- Плагины
    - Flyway, Exposed ORM
    - Ktlint, detekt
    - Kotest, Mockk
- Dpcker Desktop

DB:

- DBeaver

### Подготовка к запуску


1. В DockerDesktop в терминале запускаем следующий код (возможно заменив значения параметров):

```powershell
   docker run -d `
     --name keylearn-db `
     -e MARIADB_DATABASE=keyldb `
     -e MARIADB_USER=user `
     -e MARIADB_PASSWORD=pass `
     -e MARIADB_ROOT_PASSWORD=rootpass `
     -p 3306:3306 `
     -v mariadb_data:/var/lib/mysql `
     mariadb:latest
```

2. Скопируйте шаблон настроек подключения (можно вручную, можно через терминал IntellijIDEA)
    ```bash
    cp app.properties.example app.properties
    ```
3. При необходимости внесите изменения в вашу копию файла (в соответствии с изменёнными значениями на шаге 1)

### Применение миграций
Для вывода справки необходимо выполнить следующую команду:
```bash
./gradlew FlywayMigrator
```
Для применения миграций введите:
```bash
./gradlew FlywayMigrator --args=migrate
```
Для вывода текущей миграции в бд и количества миграций, которые ожидают применения, введите:
```bash
./gradlew FlywayMigrator --args=info
```
Для синхронизации миграций (выравнивания чек-сумм) введите:
```bash
./gradlew FlywayMigrator --args=repair
```
Для полной очистки базы данных введите:
```bash
./gradlew FlywayMigrator --args=clean
```

### Запуск приложения
Выполните задачу
```bash
./gradlew run
```

### Выполнение тестов
```bash
./gradlew test --rerun-tasks
```

### Выполнение проверок качества исходного кода
```bash
./gradlew detekt
./gradlew ktlintCheck
```
