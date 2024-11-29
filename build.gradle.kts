// import org.jreleaser.model.Active

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.7.3" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    `maven-publish` // Add ./gradlew publishToMavenLocal
    signing // Add ./gradlew signArchives
}

group = "fr.formiko.mc.biomeutils"
version = "1.1.2"
description="Tools for Minecraft plugins about biomes."
// name = "BiomeUtils"

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
    withJavadocJar()
    withSourcesJar()
}

tasks {
    assemble {
        // dependsOn(shadowJar) // Not needed, probably because reobfJar depends on shadowJar
        dependsOn(reobfJar)
    }
    publish {
        dependsOn(reobfJar)
    }
}

afterEvaluate {
    tasks.withType(PublishToMavenRepository::class.java) {
        dependsOn(tasks.assemble)
    }
}

fun addReobfTo(target: NamedDomainObjectProvider<Configuration>, classifier: String? = null) {
    target.get().let {
        it.outgoing.artifact(tasks.reobfJar.get().outputJar) {
            this.classifier = classifier
        }
        (components["java"] as AdhocComponentWithVariants).addVariantsFromConfiguration(it) {}
    }
}

addReobfTo(configurations.apiElements)
addReobfTo(configurations.runtimeElements)

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])
      // artifact(tasks["sourcesJar"])


      // TODO exclude the -dev jar
      artifactId = project.name.lowercase()
    // }
    // withType<MavenPublication> {
      pom {
        packaging = "jar"
        url.set("https://github.com/HydrolienF/BiomeUtils")
        inceptionYear.set("2024")
        description = project.description
        licenses {
          license {
            name.set("MIT license")
            url.set("https://github.com/HydrolienF/BiomeUtils/blob/master/LICENSE.md")
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
          connection.set("scm:git:git@github.com:HydrolienF/BiomeUtils.git")
          developerConnection.set("scm:git:ssh:git@github.com:HydrolienF/BiomeUtils.git")
          url.set("https://github.com/HydrolienF/BiomeUtils")
        }
      }
    }
  }
  repositories {
    maven {
        // url = layout.buildDirectory.dir("build/libs/").get().asFile.toURI()
        url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
    }
  }
}