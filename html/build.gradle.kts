plugins {
    kotlin("jvm")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

// TeaVM ships browser-friendly stubs for a handful of java.util.concurrent.*
// classes via its standard "emu" package convention. Classes live under
// emu.java.* and src/main/resources/META-INF/teavm.properties maps the
// emu.java prefix back to java.* at TeaVM compile time. The stubs are NOT on
// the runtime classpath of any other module.

private val webLauncherClassName = "ru.fredboy.cavedroid.html.WebLauncher"
private val webBuildClassName = "ru.fredboy.cavedroid.html.BuildWebJs"

dependencies {
    useCommonLibs()
    useGdxModule()
    useLightingTint()
    useTeaVMBackend()

    implementation(Dependencies.LibGDX.gdx)
}

tasks.register<JavaExec>("buildJs") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM and write build/dist."
    dependsOn("assemble")
    mainClass.set(webBuildClassName)
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = layout.projectDirectory.asFile
    systemProperty("cavedroid.assetsPath", rootProject.file("assets").absolutePath)
    systemProperty("cavedroid.launcherClass", webLauncherClassName)
}

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Build the JS bundle and start the embedded Jetty dev server."
    dependsOn("assemble")
    mainClass.set(webBuildClassName)
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = layout.projectDirectory.asFile
    systemProperty("cavedroid.assetsPath", rootProject.file("assets").absolutePath)
    systemProperty("cavedroid.launcherClass", webLauncherClassName)
    systemProperty("cavedroid.serve", "true")
}
