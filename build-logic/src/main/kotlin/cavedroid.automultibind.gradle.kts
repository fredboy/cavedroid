import org.gradle.accessors.dm.LibrariesForLibs

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
