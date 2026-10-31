import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

// Browser-friendly java.util.concurrent.* stubs live under emu.java.*.
// WebSubstitutionPolicy registers them through TeaVM's SubstitutionPolicy SPI.
// They are not on the runtime classpath of any other module.

private val webLauncherClassName = "ru.fredboy.cavedroid.html.WebLauncher"
private val webBuildClassName = "ru.fredboy.cavedroid.html.BuildWeb"

private val extraAssetsDir = layout.buildDirectory.dir("generated/extraAssets")
private val yandexExtraAssetsDir = layout.buildDirectory.dir("generated/extraAssetsYandex")

// Yandex Games 8.4.2 forbids any links to external resources (including bare
// domain names like "freesound.org"). We rewrite known license URLs to short
// names, then strip every remaining http(s) URL and bare domain so the
// notices / attributions screens stay license-compliant without leaking URLs.
private val yandexLicenseShortNames: List<Pair<Regex, String>> = listOf(
    Regex("""https?://creativecommons\.org/publicdomain/zero/1\.0/?""") to "CC0 1.0",
    Regex("""https?://creativecommons\.org/licenses/by/4\.0/?""") to "CC-BY 4.0",
    Regex("""https?://creativecommons\.org/licenses/by-sa/4\.0/?""") to "CC-BY-SA 4.0",
    Regex("""https?://creativecommons\.org/licenses/by/3\.0/?""") to "CC-BY 3.0",
    Regex("""https?://creativecommons\.org/licenses/by-sa/3\.0/?""") to "CC-BY-SA 3.0",
    Regex("""https?://(www\.)?apache\.org/licenses/LICENSE-2\.0(\.txt|\.html)?""") to "Apache 2.0",
    Regex("""https?://(www\.)?fsf\.org/licensing/licenses/lgpl\.txt""") to "LGPL 2.1",
    Regex("""https?://opensource\.org/licenses/MIT""") to "MIT",
    Regex("""https?://opensource\.org/licenses/BSD-3-Clause""") to "BSD-3-Clause",
    Regex("""https?://opensource\.org/licenses/EPL-2\.0""") to "EPL 2.0",
)

private val yandexUrlRegex = Regex("""https?://\S+""")

private val yandexBareDomainRegex = Regex(
    """\b(?:[a-zA-Z0-9-]+\.)+(?:org|com|net|io|info|dev|co|gg|games|app|me|tv|us|uk|ru)\b(?:/\S*)?""",
)

private fun String.sanitizeForYandex(): String {
    val cleaned = lineSequence()
        .map { line ->
            var l = line
            yandexLicenseShortNames.forEach { (re, name) -> l = re.replace(l, name) }
            l = yandexUrlRegex.replace(l, "")
            l = yandexBareDomainRegex.replace(l, "")
            l.trimEnd().trimEnd('-', ' ').trimEnd()
        }
        .filter { !it.trim().endsWith("URL:") }
        .joinToString("\n")
    return Regex("""\n{3,}""").replace(cleaned, "\n\n").trim() + "\n"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.bundles.common)
    implementation(project(":core:gdx"))
    implementation(project(":core:gameplay:lighting-bfs"))
    implementation(libs.gdx.backend.web)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.gdx.core)
}

// TeaVM 0.15 fails in Wasm CoroutineTransformation for this application's
// coroutine code. Upgrade only the Wasm compiler/runtime; JS keeps its baseline.
private val wasmRuntimeClasspath = configurations.resolvable("wasmRuntimeClasspath") {
    extendsFrom(configurations.runtimeClasspath.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
    resolutionStrategy.eachDependency {
        if (requested.group == "org.teavm") {
            useVersion(libs.versions.teavmWasm.get())
        }
    }
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

// gdx-teavm's WebBackend copies index.html into the dist but ignores arbitrary
// static files in src/main/resources/webapp. This task back-fills the rest so
// fonts (LanaPixel.ttf) and any other static webapp resources land alongside
// index.html. We deliberately use a plain task + project.copy {} instead of a
// Copy-typed task: the compiler writes to the same directory, and Gradle's
// stale-output cleanup would otherwise treat dist/webapp as exclusively this
// task's output and wipe the compiler's files.
private fun registerWebStaticAssets(taskName: String, outputPath: String) = tasks.register(taskName) {
    group = "build"
    description = "Copy non-HTML files from src/main/resources/webapp into $outputPath."
    doLast {
        copy {
            from(layout.projectDirectory.dir("src/main/resources/webapp")) {
                exclude("*.html")
            }
            into(layout.buildDirectory.dir("$outputPath/webapp"))
        }
    }
}

private val copyWebStaticAssets = registerWebStaticAssets("copyWebStaticAssets", "dist")
private val copyWasmStaticAssets = registerWebStaticAssets("copyWasmStaticAssets", "dist/wasm")

private fun JavaExec.configureWebBuild(
    sourceMaps: Boolean,
    obfuscate: Boolean,
    optimization: String,
    wasm: Boolean = false,
) {
    dependsOn("assemble", "copyLicenseReport", "generateAttributionIndex")
    // Copy before compilation so static resources are available while run tasks serve.
    dependsOn(if (wasm) copyWasmStaticAssets else copyWebStaticAssets)
    mainClass.set(webBuildClassName)
    classpath = if (wasm) {
        sourceSets["main"].output + wasmRuntimeClasspath.get()
    } else {
        sourceSets["main"].runtimeClasspath
    }
    workingDir = layout.projectDirectory.asFile
    systemProperty("cavedroid.assetsPath", rootProject.file("assets").absolutePath)
    systemProperty("cavedroid.extraAssetsPath", extraAssetsDir.get().asFile.absolutePath)
    systemProperty("cavedroid.launcherClass", webLauncherClassName)
    systemProperty("cavedroid.sourceMaps", sourceMaps.toString())
    systemProperty("cavedroid.obfuscate", obfuscate.toString())
    systemProperty("cavedroid.optimization", optimization)
    systemProperty("cavedroid.wasm", wasm.toString())
    systemProperty("cavedroid.outputPath", layout.buildDirectory.dir(if (wasm) "dist/wasm" else "dist").get().asFile.absolutePath)
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

tasks.register<JavaExec>("buildWasm") {
    group = "build"
    description = "Compile :html to WebAssembly GC via TeaVM (dev: debug information, SIMPLE optimization)."
    configureWebBuild(sourceMaps = true, obfuscate = false, optimization = "SIMPLE", wasm = true)
}

tasks.register<JavaExec>("runWasm") {
    group = "application"
    description = "Build the dev Wasm bundle and start the embedded Jetty server."
    configureWebBuild(sourceMaps = true, obfuscate = false, optimization = "SIMPLE", wasm = true)
    systemProperty("cavedroid.serve", "true")
}

tasks.register<JavaExec>("buildWasmRelease") {
    group = "build"
    description = "Compile :html to WebAssembly GC via TeaVM (release: SIMPLE optimization, no debug information)."
    configureWebBuild(sourceMaps = false, obfuscate = true, optimization = "SIMPLE", wasm = true)
}

tasks.register<Zip>("packageWebDistWasm") {
    group = "distribution"
    description = "Package the release Wasm web bundle into a zip ready for static hosting."
    dependsOn("buildWasmRelease")

    archiveBaseName.set("cavedroid-web-wasm")
    archiveVersion.set(ApplicationInfo.versionName)
    destinationDirectory.set(layout.buildDirectory.dir("dist"))

    from(layout.buildDirectory.dir("dist/wasm/webapp")) {
        // A previous dev build may have left debug sidecars in this directory.
        exclude("WEB-INF/**", "src/**", "**/*.map", "**/*.teadbg")
    }
}

tasks.register("sanitizeYandexAssets") {
    group = "assets"
    description = "Strip URLs and bare domains from notices.txt and per-asset attribution.txt files for Yandex Games (8.4.2) compliance."

    dependsOn("copyLicenseReport", "generateAttributionIndex")

    val noticesIn = extraAssetsDir.map { it.file("notices.txt") }
    val indexIn = extraAssetsDir.map { it.file("attribution_index.txt") }
    val assetsDir = rootProject.file("assets")
    val outDir = yandexExtraAssetsDir

    inputs.file(noticesIn)
    inputs.file(indexIn)
    inputs.dir(assetsDir)
    outputs.dir(outDir)

    doLast {
        val outRoot = outDir.get().asFile
        outRoot.mkdirs()

        outRoot.resolve("notices.txt").writeText(
            noticesIn.get().asFile.readText().sanitizeForYandex(),
        )

        val combined = buildString {
            indexIn.get().asFile.readLines()
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach { relPath ->
                    val file = assetsDir.resolve(relPath)
                    if (!file.isFile) return@forEach
                    append(relPath).append("\n\n")
                    append(file.readText().sanitizeForYandex().trim())
                    append("\n\n================\n\n")
                }
        }.trim() + "\n"

        outRoot.resolve("attributions.txt").writeText(combined)
    }
}

tasks.register<JavaExec>("buildJsYandex") {
    group = "build"
    description = "Compile :html to JavaScript via TeaVM for Yandex Games (obfuscated; notices and attributions sanitized to comply with 8.4.2)."
    configureWebBuild(sourceMaps = false, obfuscate = true, optimization = "SIMPLE")
    dependsOn("sanitizeYandexAssets")
    // Override the extra assets path set by configureWebBuild so the bundled
    // notices.txt has URLs stripped and the viewmodel picks up the combined
    // sanitized attributions.txt instead of walking attribution_index.txt.
    systemProperty("cavedroid.extraAssetsPath", yandexExtraAssetsDir.get().asFile.absolutePath)
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
