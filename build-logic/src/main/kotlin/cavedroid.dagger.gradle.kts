import org.gradle.accessors.dm.LibrariesForLibs

// Convention for modules using Dagger via KSP. Apply alongside `cavedroid.kotlin-library`.
// Replaces buildSrc's useDagger() helper.

plugins {
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    add("ksp", libs.dagger.compiler)
}

// `implementation` exists only once a JVM/Kotlin plugin is applied; guard so this
// convention is order-independent relative to cavedroid.kotlin-library.
plugins.withId("org.jetbrains.kotlin.jvm") {
    dependencies {
        add("implementation", libs.dagger)
    }
}
