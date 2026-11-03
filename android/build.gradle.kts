import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.task.ReportTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.Properties

private val natives by configurations.creating

plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

private val keystorePropertiesFile = rootProject.file("keystore.properties")
private val keystoreProperties = if (keystorePropertiesFile.exists()) {
    Properties().apply {
        load(FileInputStream(keystorePropertiesFile))
    }
} else {
    null
}

private val yandexPropertiesFile = rootProject.file("yandex.properties")
private val yandexProperties = if (yandexPropertiesFile.exists()) {
    Properties().apply {
        load(FileInputStream(yandexPropertiesFile))
    }
} else {
    null
}

android {
    namespace = ApplicationInfo.packageName
    compileSdk = 36

    sourceSets {

        named("main") {
            jniLibs.srcDir(layout.buildDirectory.dir("generated/jniLibs").get().asFile)
            assets {
                srcDirs("src/main/assets", "build/generated/extraRes/shared")
            }
        }

        named("debug") {
            res.srcDir("src/debug/res")
        }
    }

    compileOptions {
        sourceCompatibility = ApplicationInfo.sourceCompatibility
        targetCompatibility = ApplicationInfo.sourceCompatibility
    }

    defaultConfig {
        applicationId = ApplicationInfo.packageName
        minSdk = 24
        targetSdk = 36

        versionCode = ApplicationInfo.versionCode
        versionName = ApplicationInfo.versionName

        multiDexEnabled = true
    }

    flavorDimensions += "distribution"

    productFlavors {
        create("foss") {
            dimension = "distribution"

            buildConfigField("String", "BANNER_AD_UNIT_ID", "null")
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "null")
        }

        create("store") {
            dimension = "distribution"

            val bannerAdUnitId = yandexProperties?.get("bannerAdUnitId")?.toString()?.let { "\"$it\"" } ?: "null"
            val interstitialAdUnitId =
                yandexProperties?.get("interstitialAdUnitId")?.toString()?.let { "\"$it\"" } ?: "null"

            buildConfigField("String", "BANNER_AD_UNIT_ID", bannerAdUnitId)
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", interstitialAdUnitId)
        }
    }

    sourceSets {
        named("foss") {
            assets.srcDirs("build/generated/extraRes/foss")
        }
        named("store") {
            assets.srcDirs("build/generated/extraRes/store")
        }
    }

    val releaseConfig = signingConfigs.create("release_config")
    with(releaseConfig) {
        storeFile = keystoreProperties?.get("releaseKeystorePath")?.let(::file)
        storePassword = keystoreProperties?.get("releaseKeystorePassword")?.toString()
        keyAlias = keystoreProperties?.get("releaseKeyAlias")?.toString()
        keyPassword = keystoreProperties?.get("releaseKeyPassword")?.toString()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            signingConfig = releaseConfig
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        buildConfig = true
    }
}

kotlin {
    jvmToolchain(ApplicationInfo.javaVersion)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

// Sync removes obsolete libraries when native dependencies change.
tasks.register<Sync>("copyAndroidNatives") {
    into(layout.buildDirectory.dir("generated/jniLibs"))

    listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64").forEach { abi ->
        from(
            provider {
                natives.files
                    .filter { it.name.endsWith("natives-$abi.jar") }
                    .map { zipTree(it) }
            },
        ) {
            into(abi)
            include("*.so")
        }
    }
}

// AGP creates per-flavor runtime classpaths (fossReleaseRuntimeClasspath / storeReleaseRuntimeClasspath)
// but no plain `runtimeClasspath`, so the jk1 plugin's default config produces an empty report. We
// register one ReportTask per flavor with its own LicenseReportExtension, then ship the result in
// flavor-specific generated assets so each APK gets only the notices for the libs it actually bundles.
val licenseFlavors = listOf("foss", "store")

licenseFlavors.forEach { flavor ->
    val capFlavor = flavor.replaceFirstChar { it.uppercase() }
    val reportOutputDir = layout.buildDirectory.dir("reports/dependency-license-$flavor")

    val flavorLicenseConfig = LicenseReportExtension(project).apply {
        outputDir = reportOutputDir.get().asFile.absolutePath
        configurations = arrayOf("${flavor}ReleaseRuntimeClasspath")
        renderers = arrayOf(PerFlavorTextReportRenderer(reportOutputDir.get().asFile))
        excludeOwnGroup = true
        excludes = arrayOf("CaveCraft.*")
        projects = arrayOf(project)
    }

    val reportTask = tasks.register<ReportTask>("generate${capFlavor}LicenseReport") {
        config = flavorLicenseConfig
    }

    tasks.register<Copy>("copy${capFlavor}LicenseReport") {
        dependsOn(reportTask)
        from(reportOutputDir.map { it.file("THIRD-PARTY-NOTICES.txt") })
        into(layout.buildDirectory.dir("generated/extraRes/$flavor"))
        rename { "notices.txt" }
    }
}

tasks.whenTaskAdded {
    if (name.contains("package") || name.endsWith("JniLibFolders")) {
        dependsOn("copyAndroidNatives")
    }
}

tasks.register("generateAttributionIndex") {
    group = "assets"
    description = "Scans assets/ for attribution.txt files and generates attribution_index.txt"

    val assetsDir = layout.projectDirectory.dir("src/main/assets").asFile.toPath().toRealPath()
    val extraDir = layout.projectDirectory.dir("build/generated/extraRes/shared").apply {
        asFile.mkdirs()
    }
    val outputFile = extraDir.file("attribution_index.txt")

    inputs.dir(assetsDir)
    outputs.file(outputFile)

    doLast {
        val attributions = Files.walk(assetsDir)
            .filter { Files.isRegularFile(it) && it.fileName.toString().equals("attribution.txt", ignoreCase = true) }
            .map { assetsDir.relativize(it).toString().replace("\\", "/") }
            .sorted()
            .toList()

        val content = attributions.joinToString("\n")
        Files.writeString(
            outputFile.asFile.toPath(),
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
    }
}

tasks.named("preBuild") {
    dependsOn("generateAttributionIndex")
}

androidComponents {
    onVariants { variant ->
        variant.sources.assets?.addGeneratedSourceDirectory(
            rootProject.tasks.named<GenerateBgmIndex>("generateBgmIndex"),
            GenerateBgmIndex::outputDirectory,
        )
        variant.outputs.forEach { output ->
            output.outputFileName.set("android-${ApplicationInfo.versionName}.apk")
        }
    }

    licenseFlavors.forEach { flavor ->
        val capFlavor = flavor.replaceFirstChar { it.uppercase() }
        onVariants(selector().withFlavor("distribution" to flavor)) { variant ->
            val capVariant = variant.name.replaceFirstChar { it.uppercase() }
            afterEvaluate {
                tasks.named("pre${capVariant}Build") {
                    dependsOn("copy${capFlavor}LicenseReport")
                }
            }
        }
    }

    onVariants(selector().withFlavor("distribution" to "foss")) { variant ->
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        afterEvaluate {
            listOf(
                "process${variantName}GoogleServices",
                "injectCrashlyticsMappingFile$variantName",
                "uploadCrashlyticsMappingFile$variantName",
                "${variant.name}GoogleServices",
            ).forEach { tasks.findByName(it)?.enabled = false }
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data:assets"))
    implementation(libs.bundles.common)
    implementation(project(":core:gdx"))
    implementation(project(":core:gameplay:lighting-bfs"))

    implementation(libs.gdx.core)
    implementation(libs.gdx.backend.android)

    "storeImplementation"(platform(libs.firebase.bom))
    "storeImplementation"(libs.firebase.crashlytics)
    "storeImplementation"(libs.firebase.performance)
//    "storeImplementation"(libs.yandex.mobileads)

    natives(variantOf(libs.gdx.platform.core) { classifier("natives-armeabi-v7a") })
    natives(variantOf(libs.gdx.platform.core) { classifier("natives-arm64-v8a") })
    natives(variantOf(libs.gdx.platform.core) { classifier("natives-x86") })
    natives(variantOf(libs.gdx.platform.core) { classifier("natives-x86_64") })

    natives(variantOf(libs.gdx.platform.box2d) { classifier("natives-armeabi-v7a") })
    natives(variantOf(libs.gdx.platform.box2d) { classifier("natives-arm64-v8a") })
    natives(variantOf(libs.gdx.platform.box2d) { classifier("natives-x86") })
    natives(variantOf(libs.gdx.platform.box2d) { classifier("natives-x86_64") })
}
