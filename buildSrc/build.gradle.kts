plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "12.3.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Bring the jk1 license-report plugin onto buildSrc's classpath so PerFlavorTextReportRenderer
    // can reference its types at compile and runtime. The root build no longer needs a `plugins
    // { id(...) version "x" }` declaration for it — `apply(plugin = "...")` finds it via this jar.
    implementation("com.github.jk1:gradle-license-report:2.9")
}

ktlint {
    version.set("1.6.0")
}
