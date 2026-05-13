package ru.fredboy.cavedroid.html

import co.touchlab.kermit.Severity
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
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
            isTouchScreen = false,
            isDebug = false,
            preferencesStore = WebPreferencesStore(),
            lightingSystemFactory = TintLightingSystemFactory(),
            loggingSeverity = Severity.Info,
        )

        WebApplication(app, config)
    }
}
