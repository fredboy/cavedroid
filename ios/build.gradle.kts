import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.nio.file.Files
import java.nio.file.StandardOpenOption

plugins {
    kotlin("jvm")
    id("robovm")
}

java.sourceCompatibility = ApplicationInfo.sourceCompatibility
java.targetCompatibility = ApplicationInfo.sourceCompatibility

dependencies {
    useCommonLibs()
    useGdxModule()

    implementation(Dependencies.LibGDX.gdx)
    implementation(Dependencies.RoboVM.rt)
    implementation(Dependencies.RoboVM.cocoatouch)
    implementation(Dependencies.LibGDX.Ios.backend)
    implementation(Dependencies.LibGDX.Ios.natives)
    implementation(Dependencies.LibGDX.Box2d.Natives.ios)
}

val generatePlist by tasks.registering {
    doLast {
        val outputPlist = file("$buildDir/generated/Info.plist.xml")
        outputPlist.parentFile.mkdirs()
        outputPlist.writeText(
            """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
                <plist version="1.0">
                <dict>
                    <key>CFBundleDevelopmentRegion</key>
                    <string>ru</string>
                    <key>CFBundleDisplayName</key>
                    <string>${ApplicationInfo.name}</string>
                    <key>CFBundleExecutable</key>
                    <string>IOSLauncher</string>
                    <key>CFBundleIdentifier</key>
                    <string>${ApplicationInfo.packageName}</string>
                    <key>CFBundleInfoDictionaryVersion</key>
                    <string>6.0</string>
                    <key>CFBundleName</key>
                    <string>${ApplicationInfo.name}</string>
                    <key>CFBundlePackageType</key>
                    <string>APPL</string>
                    <key>CFBundleShortVersionString</key>
                    <string>${ApplicationInfo.versionName}</string>
                    <key>CFBundleSignature</key>
                    <string>????</string>
                    <key>CFBundleVersion</key>
                    <string>${ApplicationInfo.versionCode}</string>
                    <key>LSRequiresIPhoneOS</key>
                    <true/>
                    <key>UIViewControllerBasedStatusBarAppearance</key>
                    <false/>
                    <key>UIStatusBarHidden</key>
                    <true/>
                    <key>MinimumOSVersion</key>
                    <string>12.0</string>
                    <key>UIDeviceFamily</key>
                    <array>
                        <integer>1</integer>
                        <integer>2</integer>
                    </array>
                    <key>UIRequiredDeviceCapabilities</key>
                    <array>
                        <string>opengles-2</string>
                    </array>
                    <key>UISupportedInterfaceOrientations</key>
                    <array>
                        <string>UIInterfaceOrientationLandscapeLeft</string>
                        <string>UIInterfaceOrientationLandscapeRight</string>
                    </array>
                    <key>CFBundleIconName</key>
                    <string>AppIcon</string>
                </dict>
                </plist>
            """.trimIndent(),
        )
    }
}

tasks.matching { it.name.startsWith("createIpa") || it.name.startsWith("launchIPhone") }
    .configureEach { dependsOn(generatePlist) }

tasks.register<Copy>("copyLicenseReport") {
    dependsOn("generateLicenseReport")

    from("build/reports/dependency-license/THIRD-PARTY-NOTICES.txt")
    into("build/generated/extraRes")
    rename { "notices.txt" }
}

tasks.register("generateAttributionIndex") {
    group = "assets"
    description = "Scans assets/ for attribution.txt files and generates attribution_index.txt"

    val assetsDir = layout.projectDirectory.dir("../assets").asFile.toPath().toRealPath()
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

tasks.assemble.apply {
    dependsOn("copyLicenseReport")
    dependsOn("generateAttributionIndex")
}

robovm {
    isIosSkipSigning = true
}
