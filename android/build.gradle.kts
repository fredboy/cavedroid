import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.github.jk1.license.LicenseReportExtension
import com.github.jk1.license.task.ReportTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.Properties

private val natives by configurations.creating

plugins {
    id("com.android.application")
    id("kotlin-android")

    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
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
            jniLibs.srcDir("libs")
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
        minSdk = 23
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

    applicationVariants.asSequence()
        .flatMap { variant -> variant.outputs.asSequence() }
        .mapNotNull { output -> output as? com.android.build.gradle.internal.api.BaseVariantOutputImpl }
        .forEach { output -> output.outputFileName = "android-${ApplicationInfo.versionName}.apk" }

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

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

// called every time gradle gets executed, takes the native dependencies of
// the natives configuration, and extracts them to the proper libs/ folders
// so they get packed with the APK.
task("copyAndroidNatives") {
    doFirst {
        val armeabiV7Dir = file("libs/armeabi-v7a/").apply { mkdirs() }
        val arm64Dir = file("libs/arm64-v8a/").apply { mkdirs() }
        val x86Dir = file("libs/x86/").apply { mkdirs() }
        val amd64Dir = file("libs/x86_64/").apply { mkdirs() }

        natives.files.forEach { jar ->
            val outputDir = when {
                jar.name.endsWith("natives-armeabi-v7a.jar") -> armeabiV7Dir
                jar.name.endsWith("natives-arm64-v8a.jar") -> arm64Dir
                jar.name.endsWith("natives-x86.jar") -> x86Dir
                jar.name.endsWith("natives-x86_64.jar") -> amd64Dir
                else -> null
            }

            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
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

tasks.preBuild.apply {
    dependsOn("generateAttributionIndex")
}

androidComponents {
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
    useCommonLibs()
    useGdxModule()
    useLightingBox2d()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Android.backend)

    "storeImplementation"(platform(Dependencies.Google.Firebase.bom))
    "storeImplementation"(Dependencies.Google.Firebase.crashlytics)
    "storeImplementation"(Dependencies.Yandex.mobileads)

    natives(Dependencies.LibGDX.Android.Natives.armeabi)
    natives(Dependencies.LibGDX.Android.Natives.arm64)
    natives(Dependencies.LibGDX.Android.Natives.x86)
    natives(Dependencies.LibGDX.Android.Natives.x86_64)

    natives(Dependencies.LibGDX.Box2d.Natives.Android.armeabi)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.arm64)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.x86)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.x86_64)
}
