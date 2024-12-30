// import org.jreleaser.model.Active

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.8" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    `maven-publish` // Add ./gradlew publishToMavenLocal
    signing // Add ./gradlew signArchives
}

group = "fr.formiko.mc.biomeutils"
version = "1.1.10"
description="Tools for Minecraft plugins about biomes."
// name = "BiomeUtils"

repositories {
    mavenLocal()
    mavenCentral()
    maven ("https://jitpack.io")
}


dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT") // paperweight
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withJavadocJar()
    withSourcesJar()
}

  // Don't make assemble or publish depend on reobfJar We don't want to reobfuscate the jar here. It will be done later by the plugin using this dependency.

afterEvaluate {
    tasks.withType(PublishToMavenRepository::class.java) {
        dependsOn(tasks.assemble)
    }
}

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])

      artifactId = project.name.lowercase()
      pom {
        packaging = "jar"
        url.set("https://github.com/HydrolienF/${project.name}")
        inceptionYear.set("2024")
        description = project.description
        licenses {
          license {
            name.set("MIT license")
            url.set("https://github.com/HydrolienF/${project.name}/blob/master/LICENSE.md")
          }
        }
        developers {
          developer {
            id.set("hydrolienf")
            name.set("HydrolienF")
            email.set("hydrolien.f@gmail.com")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:HydrolienF/${project.name}.git")
          developerConnection.set("scm:git:ssh:git@github.com:HydrolienF/${project.name}.git")
          url.set("https://github.com/HydrolienF/${project.name}")
        }
      }
    }
  }
  repositories {
    maven {
        url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()

    }
  }
}