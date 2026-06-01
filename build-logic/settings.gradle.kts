// build-logic — included build hosting CaveDroid's Gradle convention plugins (E0.2, #151).
// Kept as a separate build (not buildSrc) so its plugins are applied explicitly per module
// and so the project's version catalog can be reused here.

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
