plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    // Gradle plugins that the convention plugins apply by id must be on the classpath here.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)

    // Expose the version catalog (`libs`) to precompiled convention plugins via
    // `the<LibrariesForLibs>()`. Standard kotlin-dsl workaround.
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
