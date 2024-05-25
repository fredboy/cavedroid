plugins {
    id("kotlin")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

private val desktopLauncherClassName = "ru.deadsoftware.cavedroid.desktop.DesktopLauncher"

tasks.register<JavaExec>("run") {
    dependsOn("build")
    mainClass = desktopLauncherClassName
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = sourceSets["main"].resources.sourceDirectories.first()
    args("--debug")
}

tasks.register<JavaExec>("runTouch") {
    dependsOn("build")
    mainClass = desktopLauncherClassName
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = sourceSets["main"].resources.sourceDirectories.first()
    args("--touch", "--debug")
}

tasks.register<Jar>("dist") {
    dependsOn("build")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = desktopLauncherClassName
    }
    from(configurations.runtimeClasspath.get().resolve().map { it.takeIf(File::isDirectory) ?: zipTree(it) })
}

dependencies {
    implementation(project(":core"))

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Desktop.backend)
    implementation(Dependencies.LibGDX.Desktop.natives)
}
