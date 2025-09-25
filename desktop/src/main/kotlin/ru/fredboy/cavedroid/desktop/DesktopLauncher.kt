package ru.fredboy.cavedroid.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.utils.Os
import com.badlogic.gdx.utils.SharedLibraryLoader
import org.lwjgl.system.Configuration
import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
import ru.fredboy.cavedroid.desktop.utils.SaveSizePrefsGameDecorator
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

internal object DesktopLauncher {

    @JvmStatic
    fun main(arg: Array<String>) {
        if (SharedLibraryLoader.os == Os.MacOsX) {
            Configuration.GLFW_LIBRARY_NAME.set("glfw_async")
        }

        val config = Lwjgl3ApplicationConfiguration()

        val preferencesStore = DesktopPreferencesStore()

        with(config) {
            setWindowIcon(
                /* fileType = */ Files.FileType.Internal,
                "icons/icon512.png",
                "icons/icon256.png",
                "icons/icon128.png",
            )
            setTitle("CaveDroid")
            setWindowedMode(
                preferencesStore.getPreference(PreferenceKeys.WINDOW_WIDTH_KEY)?.toIntOrNull() ?: 960,
                preferencesStore.getPreference(PreferenceKeys.WINDOW_HEIGHT_KEY)?.toIntOrNull() ?: 540,
            )
            useVsync(true)
        }

        var touch = false
        var debug = false

        for (anArg in arg) {
            if (anArg == "--touch") {
                touch = true
            }

            if (anArg == "--debug") {
                debug = true
            }
        }

        val caveGame = CaveDroidApplication(
            gameDataDirectoryPath = System.getProperty("user.home") + "/.cavedroid",
            isTouchScreen = touch,
            isDebug = debug,
            preferencesStore = preferencesStore,
        )

        Lwjgl3Application(SaveSizePrefsGameDecorator(caveGame), config)
    }
}
