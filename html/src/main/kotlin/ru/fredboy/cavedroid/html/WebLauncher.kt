package ru.fredboy.cavedroid.html

import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.github.xpenatan.gdx.teavm.backends.web.WebApplication
import com.github.xpenatan.gdx.teavm.backends.web.WebApplicationConfiguration
import com.github.xpenatan.gdx.teavm.backends.web.utils.WebNavigator
import org.teavm.jso.JSBody
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.api.NoOpAdController
import ru.fredboy.cavedroid.common.api.NoOpCloudStatsSync
import ru.fredboy.cavedroid.common.api.NoOpInlineTextInput
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.gameplay.lighting.tint.TintLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import java.util.Locale

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
            showDownloadLogs = false
        }

        val yandexAvailable = YandexGamesBridge.isAvailable()
        val adController: AdController = if (yandexAvailable) YandexGamesAdController() else NoOpAdController()
        val cloudStatsSync: CloudStatsSync = if (yandexAvailable) YandexCloudStatsSync() else NoOpCloudStatsSync()

        val isMobileBrowser = isMobileBrowser()

        val app = CaveDroidApplication(
            gameDataDirectoryPath = "",
            gameDataFileType = Files.FileType.Local,
            isTouchScreen = isMobileBrowser,
            isDebug = false,
            preferencesStore = WebPreferencesStore(),
            lightingSystemFactory = TintLightingSystemFactory(),
            dispatchers = AppDispatchers(
                io = GdxMainDispatcher,
                background = GdxMainDispatcher,
                main = GdxMainDispatcher,
            ),
            adController = adController,
            cloudStatsSync = cloudStatsSync,
            inlineTextInput = if (isMobileBrowser) {
                WebInlineTextInput()
            } else {
                NoOpInlineTextInput
            },
            defaultLocaleProvider = { defaultLocale(yandexAvailable) },
            loggingSeverity = Severity.Info,
            isYandexGamesBuild = yandexAvailable,
        ).let { app ->
            if (yandexAvailable) {
                YandexGamesLifecycleGameDecorator(app)
            } else {
                app
            }
        }

        WebApplication(app, config)
    }

    private fun isMobileBrowser(): Boolean {
        val userAgent = WebNavigator.getUserAgent() ?: return false
        return MOBILE_USER_AGENT_REGEX.containsMatchIn(userAgent)
    }

    private fun defaultLocale(yandexAvailable: Boolean): Locale? {
        if (yandexAvailable) {
            localeFromTag(YandexGamesBridge.getLanguage())?.let { return it }
        }
        return localeFromTag(navigatorLanguage())
    }

    private fun localeFromTag(tag: String?): Locale? {
        val safeTag = tag?.takeIf { it.isNotEmpty() } ?: return null
        val language = safeTag.substringBefore('-').substringBefore('_').lowercase()
        return language.takeIf { it.isNotEmpty() }?.let(::Locale)
    }

    @JvmStatic
    @JSBody(script = "return navigator.language;")
    private external fun navigatorLanguage(): String?

    private val MOBILE_USER_AGENT_REGEX =
        Regex("Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini", RegexOption.IGNORE_CASE)
}
