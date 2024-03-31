plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.22"
    id("java-library")
    id("maven-publish")
}

group = "eu.shoroa"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:2.3.9")
    implementation("io.ktor:ktor-client-cio:2.3.9")
    implementation("io.ktor:ktor-client-okhttp:2.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.3.9")
}

kotlin {
    jvmToolchain(17)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}