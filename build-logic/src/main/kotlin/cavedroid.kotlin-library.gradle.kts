import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Convention for a plain Kotlin/JVM library module: Kotlin + ktlint, JVM target,
// and JUnit Jupiter + mockk + coroutines-test wired by default (testing is a 2.0
// cross-cutting requirement — ADR, issue #150/#151). JVM stays 17 here; the JDK 25
// toolchain / Java 8 target bump is a separate step (E0.6, #155).

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
