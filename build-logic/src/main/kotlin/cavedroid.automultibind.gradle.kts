import org.gradle.accessors.dm.LibrariesForLibs

// Adds the in-house automultibind annotations + KSP processor. Replaces buildSrc's
// useAutomultibind(). Requires KSP (apply cavedroid.dagger or the ksp plugin alongside).

val libs = the<LibrariesForLibs>()

plugins.withId("com.google.devtools.ksp") {
    dependencies {
        add("ksp", libs.automultibind.ksp)
    }
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    dependencies {
        add("implementation", libs.automultibind.annotations)
    }
}
