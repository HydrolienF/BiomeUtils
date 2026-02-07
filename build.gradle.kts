import org.jreleaser.model.Active
import org.jreleaser.model.Signing

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    // id("com.vanniktech.maven.publish") version "0.35.0"
    `maven-publish` // Add ./gradlew publishToMavenLocal
    signing // Add ./gradlew signArchives
    id("org.jreleaser") version "1.15.0"
}

group = "fr.formiko.mc.biomeutils"
version = "1.1.14"
description="Tools for Minecraft plugins about biomes."
// name = "BiomeUtils"
var mainMinecraftVersion = "1.21.11"
val supportedMinecraftVersions = "$mainMinecraftVersion"

repositories {
    mavenLocal()
    mavenCentral()
    maven ("https://jitpack.io")
}


dependencies {
    paperweight.paperDevBundle("$mainMinecraftVersion-R0.1-SNAPSHOT") // paperweight
}

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withJavadocJar()
    withSourcesJar()
}

  // Don't make assemble or publish depend on reobfJar We don't want to reobfuscate the jar here. It will be done later by the plugin using this dependency.


publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      from(components["java"])

      artifactId = project.name.lowercase()
      pom {
        name.set(project.name.lowercase())
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
            name = "PreDeploy"
            url = uri(layout.buildDirectory.dir("pre-deploy"))
        }
  }
}


jreleaser {
    project {
        name.set("${project.name}")
        copyright.set("Hydrolien")
        description.set(findProperty("description")?.toString() ?: "Default description")
        website.set("https://github.com/HydrolienF/${project.name}")
    }

    // Not working, we use signing task instead.
    // signing {
    //     active.set(Active.ALWAYS)
    //     armored.set(true)
    //     verify.set(false)
    //     mode.set(Signing.Mode.FILE)
    //     publicKey.set("C:/Users/x/.jreleaser/public.key")
    //     secretKey.set("C:/Users/x/.jreleaser/private.key")
    //     passphrase.set(
    //         findProperty("gpgPassphrase")?.toString()
    //             ?: System.getenv("JRELEASER_GPG_PASSPHRASE")
    //     )
    // }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    username.set(findProperty("ossrhUsername")?.toString()
                        ?: System.getenv("OSSRH_USERNAME"))
                    password.set(findProperty("ossrhPassword")?.toString()
                        ?: System.getenv("OSSRH_PASSWORD"))
                    stagingRepository("build/pre-deploy")  // call as function

                    applyMavenCentralRules = false
                }
            }
        }
    }

    release {
        github {
            enabled.set(false)
        }
    }
}

signing {
    useGpgCmd() // uses local gpg executable
    sign(publishing.publications["mavenJava"])
}

tasks.register("signArtifacts") {
    dependsOn(tasks.withType<Sign>())
}


// afterEvaluate {
//     tasks.withType(PublishToMavenRepository::class.java) {
//         dependsOn(tasks.assemble)
//     }
// }

// com.vanniktech.maven.publish never worked because of an endless loop with paper & vanniktech editing plainJavadocJar task.
// mavenPublishing {
//     coordinates(
//         groupId =  project.group.toString(),
//         artifactId = project.name.lowercase(),
//         version = project.version.toString()
//     )

//     pom {
//         name.set("${project.name}")
//         description.set(project.description)
//         inceptionYear.set("2024")
//         url.set("https://github.com/HydrolienF/${project.name}")

//         licenses {
//             license {
//                 name.set("MIT license")
//                 url.set("https://github.com/HydrolienF/${project.name}/blob/master/LICENSE.md")
//             }
//         }

//         developers {
//           developer {
//             id.set("hydrolienf")
//             name.set("HydrolienF")
//             email.set("hydrolien.f@gmail.com")
//           }
//         }

//         scm {
//             connection.set("scm:git:git@github.com:HydrolienF/${project.name}.git")
//             developerConnection.set("scm:git:ssh:git@github.com:HydrolienF/${project.name}.git")
//             url.set("https://github.com/HydrolienF/${project.name}")
//         }
//     }

//     // Configure publishing to Maven Central
//     publishToMavenCentral()

//     // Enable GPG signing for all publications
//     signAllPublications()
// }

tasks.register("echoVersion") {
    group = "documentation"
    description = "Displays the version."
    doLast {
        println("${project.version}")
    }
}

tasks.register("echoReleaseName") {
    group = "documentation"
    description = "Displays the release name."
    doLast {
        println("${project.version} [${supportedMinecraftVersions}]")
    }
}

val extractChangelog = tasks.register("extractChangelog") {
    group = "documentation"
    description = "Extracts the changelog for the current project version from CHANGELOG.md, including the version header."

    val changelog: Property<String> = project.objects.property(String::class)
    outputs.upToDateWhen { false }

    doLast {
        val version = project.version.toString()
        val changelogFile = project.file("CHANGELOG.md")

        if (!changelogFile.exists()) {
            println("CHANGELOG.md not found.")
            changelog.set("No changelog found.")
            return@doLast
        }

        val lines = changelogFile.readLines()
        val entries = mutableListOf<String>()
        var foundVersion = false

        for (line in lines) {
            when {
                // Include the version line itself
                line.trim().equals("# $version", ignoreCase = true) -> {
                    foundVersion = true
                    entries.add(line)
                }
                // Stop collecting at the next version header
                foundVersion && line.trim().startsWith("# ") -> break
                // Collect lines after the version header
                foundVersion -> entries.add(line)
            }
        }

        val result = if (entries.isEmpty()) {
            "Update to $version."
        } else {
            entries.joinToString("\n").trim()
        }

        // println("Changelog for version $version:\n$result")
        changelog.set(result)
    }

    // Make changelog accessible from other tasks
    extensions.add(Property::class.java, "changelog", changelog)
}

tasks.register("echoLatestVersionChangelog") {
    group = "documentation"
    description = "Displays the latest version change."

    dependsOn(tasks.named("extractChangelog"))

    doLast {
        println((extractChangelog.get().extensions.findByType(Property::class.java) as Property<String>).get())
    }
}