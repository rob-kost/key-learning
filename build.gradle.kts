plugins {
    application
    kotlin("jvm") version "2.3.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

application {
    mainClass.set("su.itgalley.MainKt")
}

group = "su.itgalley"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val http4kVersion = "6.38.0.0"
val exposedVersion = "1.1.1"
val jacksonVersion = "2.21"
val flywayVersion = "12.2.0"
val kotestVersion = "6.1.9"
val mockkVersion = "1.14.9"
val detectVersion = "1.23.8"
val ktlintVersion = "12.1.0"
val logbackVersion = "1.5.16"
val mariaDBVersion = "3.5.8"

dependencies {

    runtimeOnly("com.h2database:h2:2.2.224") // только для проверки работоспособности сборки
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // HTTP4K
    implementation("org.http4k:http4k-core:$http4kVersion")
    implementation("org.http4k:http4k-server-jetty:$http4kVersion")
    implementation("org.http4k:http4k-client-apache:$http4kVersion")
    implementation("org.http4k:http4k-format-jackson:$http4kVersion")

    // Драйвер для MariaDB
    implementation("org.mariadb.jdbc:mariadb-java-client:$mariaDBVersion")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Jackson (сериализация JSON)
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    // Flyway (миграции БД)
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-mysql:${flywayVersion}")

    // Тестирование
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-property:$kotestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.http4k:http4k-testing-approval:$http4kVersion")
    testImplementation("org.http4k:http4k-testing-hamkrest:$http4kVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// Настройка ktlint
ktlint {
    version.set("1.1.1") // версия ktlint
    verbose.set(true)
    android.set(false)
}

// Настройка detekt
detekt {
    config.setFrom("detekt.yml") // при необходимости кастомный конфиг
    buildUponDefaultConfig = true
}

tasks.register<JavaExec>("FlywayMigrator") {
    group = "database"
    description = "Запуск утилиты миграции базы данных"

    mainClass.set("su.itgalley.database.config.migrations.FlywayMigratorKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = project.projectDir

    val argsList = mutableListOf<String>()
    if (project.hasProperty("args")) {
        val customArgs = project.property("args").toString().split(" ").filter { it.isNotBlank() }
        argsList.addAll(customArgs)
    }
    args = argsList
}
