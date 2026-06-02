import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

ktlint {
    version.set("1.6.0")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    "testImplementation"(libs.junit.jupiter)
    "testRuntimeOnly"(libs.junit.jupiter.engine)
    "testRuntimeOnly"(libs.junit.platform.launcher)
    "testImplementation"(libs.mockk)
    "testImplementation"(libs.kotlinx.coroutines.test)
}
