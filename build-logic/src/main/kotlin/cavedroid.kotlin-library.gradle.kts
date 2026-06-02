import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.attributes.java.TargetJvmVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain(25)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

tasks.named<KotlinCompile>("compileTestKotlin").configure {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.named<JavaCompile>("compileTestJava").configure {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

listOf("testCompileClasspath", "testRuntimeClasspath").forEach { configurationName ->
    configurations.named(configurationName).configure {
        attributes.attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 17)
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
