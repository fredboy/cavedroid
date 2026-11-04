import org.gradle.api.JavaVersion

object ApplicationInfo {
    const val name = "CaveDroid"

    const val versionName = "1.6.0"
    const val versionCode = 77

    const val packageName = "ru.fredboy.cavedroid"

    const val javaVersion = 25

    // Shared bytecode must remain compatible with Android, TeaVM, and RoboVM.
    val sourceCompatibility = JavaVersion.VERSION_17
}
