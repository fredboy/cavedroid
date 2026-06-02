import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("com.google.devtools.ksp")
}

val libs = the<LibrariesForLibs>()

dependencies {
    add("ksp", libs.dagger.compiler)
}

plugins.withId("org.jetbrains.kotlin.jvm") {
    dependencies {
        add("implementation", libs.dagger)
    }
}
