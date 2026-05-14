package ru.fredboy.cavedroid.html

import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import com.github.xpenatan.gdx.teavm.backends.web.utils.WebNavigator
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.gameplay.lighting.tint.TintLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

object WebLauncher {

    @JvmStatic
    fun main(args: Array<String>) {
        // kotlinx-coroutines' "stack trace recovery" walks the JVM stack to
        // enrich exceptions. Under TeaVM that walk hits nulls and crashes
        // inside the exception-handling path, masking the actual error.
        // Disabling recovery surfaces real exceptions cleanly.
        System.setProperty("kotlinx.coroutines.stacktrace.recovery", "false")

        val config = WebApplicationConfiguration().apply {
            width = 0
            height = 0
            showDownloadLogs = true
        }

        val app = CaveDroidApplication(
            gameDataDirectoryPath = "",
            gameDataFileType = Files.FileType.Local,
            isTouchScreen = isMobileBrowser(),
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

    private fun isMobileBrowser(): Boolean {
        val userAgent = WebNavigator.getUserAgent() ?: return false
        return MOBILE_USER_AGENT_REGEX.containsMatchIn(userAgent)
    }

    private val MOBILE_USER_AGENT_REGEX =
        Regex("Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini", RegexOption.IGNORE_CASE)
}
