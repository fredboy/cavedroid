import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

plugins.withId("org.jetbrains.kotlin.jvm") {
    dependencies {
        add("implementation", libs.gdx)
        add("implementation", libs.gdx.box2d)
    }
}
