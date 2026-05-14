import java.nio.file.Files
import java.nio.file.StandardOpenOption

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

private val extraAssetsDir = layout.buildDirectory.dir("generated/extraAssets")

dependencies {
    useCommonLibs()
    useGdxModule()
    useLightingTint()
    useTeaVMBackend()

    implementation(Dependencies.LibGDX.gdx)
}

tasks.register<Copy>("copyLicenseReport") {
    dependsOn(":generateLicenseReport")

    from(rootProject.layout.buildDirectory.file("reports/dependency-license/THIRD-PARTY-NOTICES.txt"))
    into(extraAssetsDir)
    rename { "notices.txt" }
}

tasks.register("generateAttributionIndex") {
    group = "assets"
    description = "Scans assets/ for attribution.txt files and writes attribution_index.txt next to other generated assets."

    val assetsDir = rootProject.file("assets").toPath().toRealPath()
    val outDir = extraAssetsDir
    val outputFile = outDir.map { it.file("attribution_index.txt") }

    inputs.dir(assetsDir)
    outputs.file(outputFile)

    doLast {
        outDir.get().asFile.mkdirs()
        val attributions = Files.walk(assetsDir)
            .filter { Files.isRegularFile(it) && it.fileName.toString().equals("attribution.txt", ignoreCase = true) }
            .map { assetsDir.relativize(it).toString().replace("\\", "/") }
            .sorted()
            .toList()

        Files.writeString(
            outputFile.get().asFile.toPath(),
            attributions.joinToString("\n"),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
        println("attribution_index.txt generated with ${attributions.size} entries.")
    }
}

private fun JavaExec.configureWebBuild() {
    dependsOn("assemble", "copyLicenseReport", "generateAttributionIndex")
    mainClass.set(webBuildClassName)
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = layout.projectDirectory.asFile
    systemProperty("cavedroid.assetsPath", rootProject.file("assets").absolutePath)
    systemProperty("cavedroid.extraAssetsPath", extraAssetsDir.get().asFile.absolutePath)
    systemProperty("cavedroid.launcherClass", webLauncherClassName)
}

tasks.register<JavaExec>("buildJs") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM and write build/dist."
    configureWebBuild()
}

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Build the JS bundle and start the embedded Jetty dev server."
    configureWebBuild()
    systemProperty("cavedroid.serve", "true")
}
