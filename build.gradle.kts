import com.github.jk1.license.render.TextReportRenderer
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.ktlint)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    // Version comes from buildSrc's `implementation` dependency on the plugin (needed there so
    // PerFlavorTextReportRenderer can reference its types). Adding a version here would clash
    // with the existing classpath entry.
    id("com.github.jk1.dependency-license-report")
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
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
        // RoboVM's legacy plugin and ProGuard's task types require the buildscript classpath.
        classpath(libs.robovm.gradle)
        classpath(libs.proguard.gradle)
    }
}

val ktlintVersion = libs.versions.ktlint.get()

allprojects {
    version = ApplicationInfo.versionName

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "com.github.jk1.dependency-license-report")

    ktlint {
        version.set(ktlintVersion)
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        plugins.withId("org.jetbrains.kotlin.jvm") {
            extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
                jvmToolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }

            extensions.configure<JavaPluginExtension> {
                toolchain {
                    languageVersion.set(JavaLanguageVersion.of(17))
                }
            }

            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
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
