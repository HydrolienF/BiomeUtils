plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.1" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    `maven-publish` // Add ./gradlew publishToMavenLocal
}

group = "fr.formiko.biomeutils"
version = "1.0.3"
description="Tools for Minecraft plugins about biomes."

repositories {
    mavenLocal()
    mavenCentral()
    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://jitpack.io")
}


dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT") // paperweight
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
tasks.jar {
    archiveFileName.set("${project.name}-${project.version}.jar")
}