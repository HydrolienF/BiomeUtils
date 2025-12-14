# BiomeUtils
Tools for Minecraft plugins about biomes.

# Use
Use this library in a Gradle kts project with: (replace 1.1.8 with the last version from the release tab.)

```kt
repositories {
    mavenCentral()
}
```
```kt
dependencies {
    implementation("fr.formiko.mc.biomeutils:biomeutils:1.1.8")
}
```

This project is inspired by :
- [TARDISChunkGenerator](https://github.com/eccentricdevotion/TARDISChunkGenerator/blob/master/src/main/java/me/eccentric_nz/tardischunkgenerator/custombiome/BiomeHelper.java)
- [Iris](https://github.com/VolmitSoftware/Iris)
- [Underilla](https://github.com/HydrolienF/Underilla)



# Assemble, publish, sign & zip.
```sh
./gradlew clean publish
./gradlew jreleaserDeploy
```
The published lib will be available [there](https://central.sonatype.com/publishing).
