buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("com.mobidevelop.robovm:robovm-gradle-plugin:2.3.23")
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}

plugins {
    alias(libs.plugins.ktlint)
    id("cavedroid.license-report")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

allprojects {
    version = providers.gradleProperty("cavedroid.versionName").get()

    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        version.set("1.6.0")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
        maven { url = uri("https://jitpack.io") }
    }
}

val testCore = tasks.register("testCore") {
    group = "verification"
    description = "Runs JVM unit tests in all :core modules."
}

subprojects {
    if (path.startsWith(":core:")) {
        plugins.withId("org.jetbrains.kotlin.jvm") {
            testCore.configure { dependsOn(tasks.named("test")) }
        }
    }
}
