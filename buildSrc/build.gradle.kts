plugins {
    `kotlin-dsl`
    alias(libs.plugins.buildsrc.ktlint)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Bring the jk1 license-report plugin onto buildSrc's classpath so PerFlavorTextReportRenderer
    // can reference its types at compile and runtime. The root build no longer needs a `plugins
    // { id(...) version "x" }` declaration for it — `apply(plugin = "...")` finds it via this jar.
    implementation(libs.license.report)
}

ktlint {
    version.set(libs.versions.ktlint.get())
}
