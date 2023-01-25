import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version Versions.SPRING_BOOT
    id("io.spring.dependency-management") version Versions.SPRING_DEPENDENCY_MANAGEMENT
    id("com.google.cloud.tools.jib") version Versions.JIB
    kotlin("jvm") version Versions.KOTLIN
    kotlin("plugin.spring") version Versions.KOTLIN
}

group = "com.assignment"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-dao:${Versions.EXPOSED}")
    implementation("org.jetbrains.exposed:exposed-java-time:${Versions.EXPOSED}")
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.github.microutils:kotlin-logging:${Versions.KOTLIN_LOGGING}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5-jvm:${Versions.KOTEST}")
    testImplementation("org.testcontainers:postgresql:${Versions.TESTCONTAINERS}")
    testImplementation("org.testcontainers:kafka:${Versions.TESTCONTAINERS}")
    testImplementation("org.awaitility:awaitility-kotlin:${Versions.AWAITILITY}")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
