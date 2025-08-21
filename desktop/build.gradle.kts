import com.android.build.gradle.internal.tasks.factory.dependsOn
import io.github.fourlastor.construo.Target
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm")
    construo
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

private val desktopLauncherClassName = "ru.fredboy.cavedroid.desktop.DesktopLauncher"

sourceSets {
    main {
        resources {
            srcDirs("src/main/extra")
        }
    }
}

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

tasks.register<Copy>("copyLicenseReport") {
    dependsOn("generateLicenseReport")

    from("build/reports/dependency-license/THIRD-PARTY-NOTICES.txt")
    into("src/main/extra")
    rename { "notices.txt" }
}

tasks.processResources.dependsOn("copyLicenseReport")

construo {
    name.set("cavedroid")
    humanName.set(ApplicationInfo.name)
    version.set(ApplicationInfo.versionName)
    mainClass.set(desktopLauncherClassName)
    outputDir.set(layout.buildDirectory.dir("dist"))
    jarTask.set("proguard")

    targets {
        create<Target.Linux>("linuxX64") {
            architecture.set(Target.Architecture.X86_64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_x64_linux_hotspot_17.0.11_9.tar.gz")
        }
        create<Target.MacOs>("macM1") {
            architecture.set(Target.Architecture.AARCH64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_aarch64_mac_hotspot_17.0.11_9.tar.gz")
            identifier.set(ApplicationInfo.packageName)
            macIcon.set(project.file("macos/AppIcon.icns"))
        }
        create<Target.Windows>("winX64") {
            architecture.set(Target.Architecture.X86_64)
            jdkUrl.set("https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.11%2B9/OpenJDK17U-jdk_x64_windows_hotspot_17.0.11_9.zip")
            useGpuHint.set(false)
        }
    }
}

dependencies {
    useCommonModule()
    useGdxModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.LibGDX.Desktop.backend)
    implementation(Dependencies.LibGDX.Desktop.natives)
    implementation(Dependencies.LibGDX.Box2d.Natives.desktop)
}
