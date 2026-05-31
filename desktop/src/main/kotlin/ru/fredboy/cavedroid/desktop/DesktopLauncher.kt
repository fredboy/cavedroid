package ru.fredboy.cavedroid.desktop

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import kotlinx.coroutines.Dispatchers
import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.common.utils.safeCast
import ru.fredboy.cavedroid.desktop.utils.SaveSizePrefsGameDecorator
import ru.fredboy.cavedroid.gameplay.lighting.bfs.BfsLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.game.GameScreen

internal object DesktopLauncher {

    private val logger = Logger.withTag("DesktopLauncher")

    @JvmStatic
    fun main(arg: Array<String>) {
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
        var verbose = false

        for (anArg in arg) {
            if (anArg == "--touch") {
                touch = true
            }

            if (anArg == "--debug") {
                debug = true
            }

            if (anArg == "--verbose") {
                verbose = true
            }
        }

        val caveGame = CaveDroidApplication(
            gameDataDirectoryPath = System.getProperty("user.home") + "/.cavedroid",
            gameDataFileType = Files.FileType.Absolute,
            isTouchScreen = touch,
            isDebug = debug,
            preferencesStore = preferencesStore,
            lightingSystemFactory = BfsLightingSystemFactory(),
            dispatchers = AppDispatchers(
                io = Dispatchers.IO,
                background = Dispatchers.Default,
                main = GdxMainDispatcher,
            ),
            saveTransferController = DesktopSaveTransferController(),
            loggingSeverity = when {
                verbose -> Severity.Verbose
                debug -> Severity.Debug
                else -> Severity.Info
            },
        )

        if (debug) {
            startRepl(caveGame)
        }
        Lwjgl3Application(SaveSizePrefsGameDecorator(caveGame), config)
    }

    private fun startRepl(caveGame: CaveDroidApplication) {
        val replThread = Thread {
            while (!Thread.currentThread().isInterrupted) {
                val screen = caveGame.screen?.safeCast<GameScreen>() ?: continue
                val commandWithArgs = readln()

                try {
                    screen.gameComponent?.commandExecutor?.execute(commandWithArgs)
                } catch (exception: Exception) {
                    logger.w(exception) { "Exception while executing command '$commandWithArgs'" }
                }
            }
        }
        replThread.isDaemon = true
        replThread.start()
    }
}
