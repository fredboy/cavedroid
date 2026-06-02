import org.gradle.accessors.dm.LibrariesForLibs

// Adds libGDX core + Box2D. Replaces buildSrc's useLibgdx(). Apply alongside cavedroid.kotlin-library.

val libs = the<LibrariesForLibs>()

plugins.withId("org.jetbrains.kotlin.jvm") {
    dependencies {
        add("implementation", libs.gdx)
        add("implementation", libs.gdx.box2d)
    }
}
