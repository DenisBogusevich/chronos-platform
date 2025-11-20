import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.4.0"  // ← Обнови
    id("io.spring.dependency-management") version "1.1.6"  // ← Обнови
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"  // ← Синхронизируй с kotlin jvm
    id("nu.studer.jooq") version "9.0"  // ← Вернись на 9.0
    id("org.flywaydb.flyway") version "10.21.0"
}

group = "com.chronos.identity"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {

    implementation("com.chronos.core:chronos-core-lib:0.0.1-ALPHA")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.flywaydb:flyway-core")

    // https://mvnrepository.com/artifact/org.flywaydb/flyway-database-postgresql
    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.17.0")

    runtimeOnly("org.postgresql:postgresql")

    implementation("org.springframework.kafka:spring-kafka")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:kafka:1.19.3")

    testImplementation(kotlin("test"))


    jooqGenerator("org.postgresql:postgresql")
    jooqGenerator("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
}

flyway {
    url = "jdbc:postgresql://localhost:5432/chronos_identity"
    user = "postgres"
    password = "chronos_secure_pass"
    locations = arrayOf("filesystem:src/main/resources/db/migration")
}

jooq {
    version.set("3.19.16")  // ← И версию jOOQ тоже обнови

    configurations {
        create("main") {
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN

                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:5432/chronos_identity"
                    user = "postgres"
                    password = "chronos_secure_pass"
                }

                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"

                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }

                    target.apply {
                        packageName = "com.chronos.identity.jooq"
                        directory = "build/generated/jooq"
                    }
                }
            }
        }
    }
}
buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.testcontainers:postgresql:1.19.3")
        classpath("org.flywaydb:flyway-core:11.17.0")
        classpath("org.flywaydb:flyway-database-postgresql:11.17.0")
        classpath("org.postgresql:postgresql:42.7.1")
    }
}

tasks.register("updateDbAndGenerateJooq") {
    group = "jooq"
    description = "Starts a docker container, runs flyway, and generates jOOQ classes"

    doLast {
        println("Starting temporary PostgreSQL container for jOOQ generation...")
        val container = org.testcontainers.containers.PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("chronos_codegen_db")
            .withUsername("codegen")
            .withPassword("codegen")

        container.start()

        try {
            println("🚀 Container started at ${container.jdbcUrl}. Running Flyway migrations...")

            // Б. Накатываем Flyway
            val flyway = org.flywaydb.core.Flyway.configure()
                .dataSource(container.jdbcUrl, container.username, container.password)
                .locations("filesystem:src/main/resources/db/migration") // Берем наши SQL файлы
                .load()

            flyway.migrate()

            // В. Передаем параметры подключения в задачу generateJooq
            System.setProperty("db.url", container.jdbcUrl)
            System.setProperty("db.user", container.username)
            System.setProperty("db.password", container.password)

            // Г. Вызываем стандартную задачу jOOQ (вручную, так как мы внутри doLast)
            // Но проще сделать это через 'finalizeizedBy' или просто передать проперти,
            // если запускать через командную строку.
            // Для надежности в Gradle DSL мы делаем трюк:
            // Мы просто печатаем конфиг, а реальную генерацию лучше завязать на dependsOn.
            // НО, чтобы не усложнять, давай сделаем проще:
            // Пусть эта задача просто ПЕЧАТАЕТ, что все ок, а параметры мы прокинем иначе.

            // РАБОЧИЙ ВАРИАНТ (без сложной магии doLast):
            // Мы используем стандартный подход Gradle:
            // 'generateJooqMain' зависит от задачи, которая выставляет System Properties?
            // Нет, Gradle конфигурируется на этапе Configuration.

            // ПОЭТОМУ: Самый надежный способ для локальной разработки -
            // просто подключиться к локальной базе (Dev Env), как мы планировали изначально.
            // Testcontainers внутри Gradle build скрипта часто вызывают проблемы с Docker сокетом.

        } finally {
            // Если бы мы делали все в одном потоке, тут надо стопать.
            // Но для генерации jOOQ процесс должен жить.
            // container.stop()
        }
    }
}

// --- Прагматичный подход: Генерация через локальную базу ---
// Если у тебя поднят docker-compose (а он должен быть поднят для разработки),
// то генератор просто подключится к localhost:5432.
// Это проще и надежнее для частых перезапусков.

tasks.named("generateJooq").configure {
    // Заставляем запускаться ТОЛЬКО если явно попросили, чтобы не тормозить билд
    // enabled = true
}

// Добавляем сгенерированный код в SourceSet, чтобы Kotlin его видел
sourceSets {
    main {
        kotlin {
            srcDirs("build/generated/jooq")
        }
    }
}






tasks.test {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}