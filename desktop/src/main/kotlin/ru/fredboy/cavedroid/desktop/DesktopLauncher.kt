package ru.fredboy.cavedroid.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

internal object DesktopLauncher {

    @JvmStatic
    fun main(arg: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration()

        with(config) {
            setWindowIcon(
                /* fileType = */ Files.FileType.Internal,
                "icons/icon512.png",
                "icons/icon256.png",
                "icons/icon128.png",
            )
            setTitle("CaveDroid")
            setWindowedMode(960, 540)
            useVsync(true)
        }

        var touch = false
        var debug = false
        var assetsPath: String? = null

        for (anArg in arg) {
            if (anArg == "--touch") {
                touch = true
            }

            if (anArg == "--debug") {
                debug = true
            }

            if (anArg.startsWith("--assets")) {
                val splitArg: Array<String> = anArg.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (splitArg.size >= 2) {
                    assetsPath = splitArg[1]
                }
            }
        }

        val caveGame = CaveDroidApplication(
            gameDataDirectoryPath = System.getProperty("user.home") + "/.cavedroid",
            isTouchScreen = touch,
            isDebug = debug,
            preferencesStore = DesktopPreferencesStore(),
        )

        Lwjgl3Application(caveGame, config)
    }
}
