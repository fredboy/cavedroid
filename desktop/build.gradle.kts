import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.fourlastor.construo.Target
import proguard.gradle.ProGuardTask
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.Properties
import kotlin.apply

plugins {
    kotlin("jvm")
    construo
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

private val desktopLauncherClassName = "ru.fredboy.cavedroid.desktop.DesktopLauncher"

private val keystorePropertiesFile = rootProject.file("keystore.properties")

private val keystoreProperties = if (keystorePropertiesFile.exists()) {
    Properties().apply {
        load(FileInputStream(keystorePropertiesFile))
    }
} else {
    null
}

sourceSets {
    main {
        resources {
            srcDirs("build/generated/extraRes")
        }
    }
}

tasks.register<JavaExec>("run") {
    dependsOn("assemble")
    mainClass = desktopLauncherClassName
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = sourceSets["main"].resources.sourceDirectories.first()
    args("--debug")
}

tasks.register<JavaExec>("runTouch") {
    dependsOn("assemble")
    mainClass = desktopLauncherClassName
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = sourceSets["main"].resources.sourceDirectories.first()
    args("--touch", "--debug")
}

tasks.register<Jar>("dist") {
    dependsOn("assemble")
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

tasks.register<ProGuardTask>("proguard") {
    injars(tasks.named("dist"))
    outjars(layout.buildDirectory.file("libs/release-${ApplicationInfo.versionName}.jar"))

    configuration("proguard-rules.pro")

    // Java runtime libraries
    libraryjars("${System.getProperty("java.home")}/jmods/java.base.jmod")
    libraryjars("${System.getProperty("java.home")}/jmods/java.desktop.jmod")
    libraryjars("${System.getProperty("java.home")}/jmods/java.logging.jmod")
    libraryjars("${System.getProperty("java.home")}/jmods/java.prefs.jmod")
}

tasks.register<Jar>("generateSignedJar") {
    dependsOn("proguard")

    val proguardJar = layout.buildDirectory.file("libs/release-${ApplicationInfo.versionName}.jar").get().asFile
    from(zipTree(proguardJar))

    archiveBaseName.set("release-signed")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    doLast {
        requireNotNull(keystoreProperties) { "keystore.properties missing" }

        val storeFile = requireNotNull(keystoreProperties["releaseKeystorePath"]?.let(::file))
        val storePassword = requireNotNull(keystoreProperties["releaseKeystorePassword"]?.toString())
        val keyAlias = requireNotNull(keystoreProperties["releaseKeyAlias"]?.toString())
        val keyPassword = requireNotNull(keystoreProperties["releaseKeyPassword"]?.toString())

        ant.withGroovyBuilder {
            val signedJar = archiveFile.get().asFile

            "signjar"(
                "jar" to signedJar.absolutePath,
                "alias" to keyAlias,
                "keystore" to storeFile,
                "storepass" to storePassword,
                "keypass" to keyPassword,
            )
        }
    }
}

tasks.register<Copy>("copyLicenseReport") {
    dependsOn("generateLicenseReport")

    from("build/reports/dependency-license/THIRD-PARTY-NOTICES.txt")
    into("build/generated/extraRes")
    rename { "notices.txt" }
}

tasks.register("generateAttributionIndex") {
    group = "assets"
    description = "Scans assets/ for attribution.txt files and generates attribution_index.txt"

    val assetsDir = layout.projectDirectory.dir("src/main/resources").asFile.toPath().toRealPath()
    val extraDir = layout.projectDirectory.dir("build/generated/extraRes").apply {
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

        if (attributions.isEmpty()) {
            println("No attribution.txt files found in $assetsDir")
        } else {
            println("Found attribution files:")
            attributions.forEach { println(" - $it") }
        }

        val content = attributions.joinToString("\n")
        Files.writeString(
            outputFile.asFile.toPath(),
            content,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE,
        )
        println("attribution_index.txt generated with ${attributions.size} entries.")
    }
}

tasks.processResources.apply {
    dependsOn("copyLicenseReport")
    dependsOn("generateAttributionIndex")
}

construo {
    name.set("cavedroid")
    humanName.set(ApplicationInfo.name)
    version.set(ApplicationInfo.versionName)
    mainClass.set(desktopLauncherClassName)
    outputDir.set(layout.buildDirectory.dir("dist"))
    jarTask.set("generateSignedJar")

    targets {
        create<Target.Linux>("linuxX64") {
            architecture.set(Target.Architecture.X86_64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16%2B8/OpenJDK17U-jdk_x64_linux_hotspot_17.0.16_8.tar.gz")
        }
        create<Target.MacOs>("macM1") {
            architecture.set(Target.Architecture.AARCH64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16%2B8/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.16_8.tar.gz")
            identifier.set(ApplicationInfo.packageName)
            macIcon.set(project.file("macos/AppIcon.icns"))
        }
        create<Target.Windows>("winX64") {
            architecture.set(Target.Architecture.X86_64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.16%2B8/OpenJDK17U-jdk_x64_windows_hotspot_17.0.16_8.zip")
            icon.set(project.file("src/main/resources/icons/icon512.png"))
        }
    }
}

dependencies {
    useCommonLibs()
    useGdxModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Desktop.backend)
    implementation(Dependencies.LibGDX.Desktop.natives)
    implementation(Dependencies.LibGDX.Box2d.Natives.desktop)
}
