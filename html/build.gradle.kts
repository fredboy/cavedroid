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

// Source roots for TeaVM source-map generation. We feed every Kotlin/Java
// source dir of every Gradle subproject so stack traces in the browser
// resolve to the original Kotlin files instead of the generated JS.
private val sourceRoots: String by lazy {
    rootProject.subprojects
        .flatMap { project ->
            listOf(
                project.projectDir.resolve("src/main/kotlin"),
                project.projectDir.resolve("src/main/java"),
            )
        }
        .filter { it.isDirectory }
        .joinToString(File.pathSeparator) { it.absolutePath }
}

private fun JavaExec.configureWebBuild(
    sourceMaps: Boolean,
    obfuscate: Boolean,
    optimization: String,
) {
    dependsOn("assemble", "copyLicenseReport", "generateAttributionIndex")
    mainClass.set(webBuildClassName)
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = layout.projectDirectory.asFile
    systemProperty("cavedroid.assetsPath", rootProject.file("assets").absolutePath)
    systemProperty("cavedroid.extraAssetsPath", extraAssetsDir.get().asFile.absolutePath)
    systemProperty("cavedroid.launcherClass", webLauncherClassName)
    systemProperty("cavedroid.sourceMaps", sourceMaps.toString())
    systemProperty("cavedroid.obfuscate", obfuscate.toString())
    systemProperty("cavedroid.optimization", optimization)
    if (sourceMaps) {
        systemProperty("cavedroid.sourceRoots", sourceRoots)
    }
}

tasks.register<JavaExec>("buildJs") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM (dev: source maps, no obfuscation, SIMPLE optimization)."
    configureWebBuild(sourceMaps = true, obfuscate = false, optimization = "SIMPLE")
}

tasks.register<JavaExec>("runWeb") {
    group = "application"
    description = "Build the dev JS bundle and start the embedded Jetty server."
    configureWebBuild(sourceMaps = true, obfuscate = false, optimization = "SIMPLE")
    systemProperty("cavedroid.serve", "true")
}

tasks.register<JavaExec>("buildJsRelease") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM (release: obfuscated, SIMPLE optimization, no source maps)."
    configureWebBuild(sourceMaps = false, obfuscate = true, optimization = "SIMPLE")
}

tasks.register<JavaExec>("buildJsYandex") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM for Yandex Games (SIMPLE optimization, obfuscated, no source maps)."
    configureWebBuild(sourceMaps = false, obfuscate = true, optimization = "SIMPLE")
}

tasks.register<Zip>("packageWebDist") {
    group = "distribution"
    description = "Package the release web bundle into a zip ready for static hosting."
    dependsOn("buildJsRelease")

    archiveBaseName.set("cavedroid-web")
    archiveVersion.set(ApplicationInfo.versionName)
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from(layout.buildDirectory.dir("dist/webapp")) {
        // WEB-INF is only used by the embedded Jetty dev server; static
        // hosts don't need it and including it leaks the servlet config.
        exclude("WEB-INF/**")
    }
}

// Generates the Yandex Games variant of index.html into its own directory.
// We don't write into dist/webapp because that's owned by buildJsRelease and
// Gradle's Copy-task output tracking would clobber the rest of the bundle.
private val yandexOverlayDir = layout.buildDirectory.dir("generated/yandex")

tasks.register<Copy>("applyYandexIndexHtml") {
    group = "build"
    description = "Render the Yandex Games variant of index.html with template substitutions applied."

    from(layout.projectDirectory.file("src/main/resources/webapp/index-yandex.html"))
    into(yandexOverlayDir)
    rename { "index.html" }

    // Placeholders mirror what gdx-teavm's WebBackend would substitute in the
    // default index.html — we re-do that substitution here because the source
    // template still has the raw %TITLE% / %JS_SCRIPT% / %MODE% tokens.
    filter<org.apache.tools.ant.filters.ReplaceTokens>(
        "beginToken" to "%",
        "endToken" to "%",
        "tokens" to mapOf(
            "TITLE" to ApplicationInfo.name,
            "JS_SCRIPT" to "<script type=\"text/javascript\" charset=\"utf-8\" src=\"app.js\"></script>",
            "MODE" to "main()",
            "WIDTH" to "800",
            "HEIGHT" to "600",
            "ARGS" to "",
        ),
    )
}

tasks.register<Zip>("packageWebDistYandex") {
    group = "distribution"
    description = "Package the release web bundle as a Yandex Games-ready zip."
    dependsOn("buildJsYandex", "applyYandexIndexHtml")

    archiveBaseName.set("cavedroid-web-yandex")
    archiveVersion.set(ApplicationInfo.versionName)
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from(layout.buildDirectory.dir("dist/webapp")) {
        exclude("WEB-INF/**", "index.html")
    }
    from(yandexOverlayDir)
}
