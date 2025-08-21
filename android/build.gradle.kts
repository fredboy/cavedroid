import java.io.FileInputStream
import java.util.Properties

private val natives by configurations.creating

plugins {
    id("com.android.application")
    id("kotlin-android")
}

private val keystorePropertiesFile = rootProject.file("keystore.properties")
private val keystoreProperties = if (keystorePropertiesFile.exists()) {
    Properties().apply {
        load(FileInputStream(keystorePropertiesFile))
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
        minSdk = 21
        targetSdk = 36

        versionCode = ApplicationInfo.versionCode
        versionName = ApplicationInfo.versionName

        multiDexEnabled = true
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

tasks.whenTaskAdded {
    if (name.contains("package")) {
        dependsOn("copyAndroidNatives")
    }
}

dependencies {
    useCommonModule()
    useGdxModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Android.backend)

    natives(Dependencies.LibGDX.Android.Natives.armeabi)
    natives(Dependencies.LibGDX.Android.Natives.arm64)
    natives(Dependencies.LibGDX.Android.Natives.x86)
    natives(Dependencies.LibGDX.Android.Natives.x86_64)

    natives(Dependencies.LibGDX.Box2d.Natives.Android.armeabi)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.arm64)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.x86)
    natives(Dependencies.LibGDX.Box2d.Natives.Android.x86_64)
}
