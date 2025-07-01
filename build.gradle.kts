plugins {
    ktlintGradle
}

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://mvn.fredboy.ru/releases/") }
    }

    dependencies {
        classpath(Dependencies.androidGradlePlugin)
        classpath(Dependencies.Kotlin.gradlePlugin)
    }
}

allprojects {
    version = ApplicationInfo.versionName

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://mvn.fredboy.ru/releases/") }
    }
}
