import org.jreleaser.model.Active

plugins {
    `java-library`
    // id("io.github.goooler.shadow") version "8.1.7"
    id("io.papermc.paperweight.userdev") version "1.7.3" // paperweight // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
    `maven-publish` // Add ./gradlew publishToMavenLocal
    id("org.jreleaser") version "1.15.0"
    `signing` // Add ./gradlew signArchives
}

group = "fr.formiko.mc.biomeutils"
version = "1.0.45"
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
}

tasks {
    // shadowJar {
    //     // val prefix = "${project.group}.lib"
    //     // sequenceOf(
    //     // ).forEach { pkg ->
    //     //     relocate(pkg, "$prefix.$pkg")
    //     // }
    //     archiveFileName.set("${project.name}-${project.version}.jar")
    // }

    assemble {
        // dependsOn(shadowJar) // Not needed, probably because reobfJar depends on shadowJar
        dependsOn(reobfJar)
    }
    publish {
        dependsOn(reobfJar)
        // dependsOn(sign)
    }
}

afterEvaluate {
    tasks.withType(PublishToMavenRepository::class.java) {
        dependsOn(tasks.assemble)
    }
}

// paperweight.

java {
  withJavadocJar()
  withSourcesJar()
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

// tasks.jar{
//   enabled = true
//   // Remove `plain` postfix from jar file name
//   archiveClassifier.set("")
// }

publishing{
  publications {
    create<MavenPublication>("Maven") {
      from(components["java"])
      // TODO exclude the -dev jar
      artifactId = project.name.lowercase()
    }
    withType<MavenPublication> {
      pom {
        packaging = "jar"
        url.set("https://github.com/HydrolienF/BiomeUtils")
        inceptionYear.set("2024")
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


// jreleaser {
//   project {
//     copyright.set("HydrolienF")
//     // version.set(project.version.toString())
//     // name.set(project.name)
//     // description.set(project.description)
//     license.set("MIT")
//   }
//   gitRootSearch.set(true)
//   signing {
//     active = Active.ALWAYS
//     armored.set(true)
//   }
//   assemble {
//     // active = Active.ALWAYS
//   }
//   deploy {
//     maven {
//       nexus2 {
//         create("maven-central") {
//           active = Active.ALWAYS
//           url.set("https://s01.oss.sonatype.org/service/local")
//           closeRepository.set(true)
//           releaseRepository.set(true)
//           stagingRepositories.add("build/staging-deploy")
//           // stagingRepositories.add("build/libs")
//           applyMavenCentralRules.set(true)
//         }  
//       }
//     }
//   }
// }

signing {
  useGpgCmd()
  sign(configurations.archives.get())
  // Sign all file from publish task
  // sign(publishing.publications["Maven"])
  // sign(layout.buildDirectory.dir("staging-deploy").get().asFile)
  // sign(tasks["jar"]) // Sign only 1 jar
  // sign(tasks["assemble"])
  // Sign all files from assemble task
  // sign(tasks["jar"])
  // sign(tasks["javadocJar"])
  // sign(tasks["sourcesJar"])
  // sign()
}
