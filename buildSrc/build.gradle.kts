plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

repositories {
    mavenCentral()
}

ktlint {
    version.set("1.6.0")
}
