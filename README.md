# BiomeUtils
Tools for Minecraft plugins about biomes.

# Use
Use this library in a Gradle kts project with: (replace LAST_VERSION by the last version from the release tab.)

repositories {
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.HydrolienF:BiomeUtils:LAST_VERSION")
}

This project is inspired by :
- [TARDISChunkGenerator](https://github.com/eccentricdevotion/TARDISChunkGenerator/blob/master/src/main/java/me/eccentric_nz/tardischunkgenerator/custombiome/BiomeHelper.java)
- [Iris](https://github.com/VolmitSoftware/Iris)
- [Underilla](https://github.com/HydrolienF/Underilla)



# Assemble, publish, sign & zip.
// TODO instead of command line, it will be able to use gradle onces I figure out how to sign all the files & push to maven central.
// At least for now it's working
```sh
./gradlew clean publish
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2.jar
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2-javadoc.jar
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2-sources.jar
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2.pom
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2.module
gpg -ab build/staging-deploy/fr/formiko/mc/biomeutils/biomeutils/1.1.2/biomeutils-1.1.2-dev.jar
cd build/staging-deploy/
zip -r staging-deploy-1.1.2.zip fr
cd ../..
```
Then publish the .zip file [there](https://central.sonatype.com/publishing)