plugins {
    kotlin("jvm") version "2.3.0"
    id("application")
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "mif.graph"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://mvn.topobyte.de")
    maven("https://mvn.slimjars.com")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$projectDir/config/detekt/detekt.yml"))
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("de.topobyte:osm4j-core:1.4.1")
    implementation("de.topobyte:osm4j-pbf:1.4.1")
    implementation("de.topobyte:osm4j-xml:1.4.1")
    implementation("com.github.ajalt.clikt:clikt:5.0.3")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
}

application {
    mainClass.set("mif.graph.MainKt")
    applicationDefaultJvmArgs = listOf("-Xmx4g")
}

kotlin {
    jvmToolchain(22)
}

tasks.test {
    useJUnitPlatform()
}