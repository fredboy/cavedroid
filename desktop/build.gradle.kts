plugins {
    kotlin
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
    from(
        files(
            configurations.runtimeClasspath.map { classpath ->
                classpath.map { file ->
                    file.takeIf(File::isDirectory) ?: zipTree(file)
                }
            },
        ),
    )
    with(tasks.jar.get())
}

dependencies {
    useCommonModule()
    useZygoteModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Desktop.backend)
    implementation(Dependencies.LibGDX.Desktop.natives)
}
