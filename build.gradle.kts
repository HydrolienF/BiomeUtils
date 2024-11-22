plugins {
    `java-library`
    id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.3" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    `maven-publish` // Add ./gradlew publishToMavenLocal
}

group = "fr.formiko.biomeutils"
version = "1.0.28"
description="Tools for Minecraft plugins about biomes."

repositories {
    mavenLocal()
    mavenCentral()
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
    publications {
        create<MavenPublication>("maven") {
		    from(components["java"])
            artifactId = project.name.lowercase()
            // artifact shadowJar
        }
    }
}

tasks {
    shadowJar {
        // val prefix = "${project.group}.lib"
        // sequenceOf(
        // ).forEach { pkg ->
        //     relocate(pkg, "$prefix.$pkg")
        // }
        archiveFileName.set("${project.name}-${project.version}.jar")
    }

    assemble {
        // dependsOn(shadowJar) // Not needed, probably because reobfJar depends on shadowJar
        dependsOn(reobfJar)
    }
}