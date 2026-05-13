package ru.fredboy.cavedroid.html

import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.gameplay.lighting.tint.TintLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

object WebLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        val config = WebApplicationConfiguration().apply {
            width = 0
            height = 0
            showDownloadLogs = true
        }

        val app = CaveDroidApplication(
            gameDataDirectoryPath = "",
            gameDataFileType = Files.FileType.Local,
            isTouchScreen = false,
            isDebug = false,
            preferencesStore = WebPreferencesStore(),
            lightingSystemFactory = TintLightingSystemFactory(),
            dispatchers = AppDispatchers(
                io = GdxMainDispatcher,
                background = GdxMainDispatcher,
                main = GdxMainDispatcher,
            ),
            loggingSeverity = Severity.Info,
        )

        WebApplication(app, config)
    }
}
