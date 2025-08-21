import com.github.jk1.license.render.TextReportRenderer

plugins {
    ktlintGradle
    dependencyLicenseReport
}

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
        classpath(Dependencies.androidGradlePlugin)
        classpath(Dependencies.Kotlin.gradlePlugin)
        classpath(Dependencies.RoboVM.gradlePlugin)
        classpath(Dependencies.proGuardPlugin)
    }
}

allprojects {
    version = ApplicationInfo.versionName

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.jk1.dependency-license-report")

    ktlint {
        version.set("1.6.0")
    }

    licenseReport {
        excludeOwnGroup = true
        renderers = arrayOf(TextReportRenderer())
        excludes = arrayOf("CaveCraft.*")
    }

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven { url = uri("https://oss.sonatype.org/content/repositories/releases/") }
        maven { url = uri("https://jitpack.io") }
    }
}
