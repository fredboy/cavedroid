plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
