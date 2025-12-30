plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.22"
}

group = "com.seuapp.financas"
version = "0.0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("finance.ApplicationKt")
}

ktor {
    fatJar {
        archiveFileName.set("app.jar")
    }
}

dependencies {
    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    
    // Content Negotiation - JSON
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    
    // Plugins Ktor
    implementation("io.ktor:ktor-server-cors-jvm")
    implementation("io.ktor:ktor-server-status-pages-jvm")
    
    // Database - Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.46.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.46.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.46.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.46.0")
    
    // Database Driver - PostgreSQL
    implementation("org.postgresql:postgresql:42.7.1")
    
    // HikariCP - Pool de Conexões ← ESTA LINHA É ESSENCIAL!
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Segurança - BCrypt
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Utilitários
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testes
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
