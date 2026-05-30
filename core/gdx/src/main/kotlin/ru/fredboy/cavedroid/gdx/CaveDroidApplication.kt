package ru.fredboy.cavedroid.gdx

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.badlogic.gdx.Application
import com.badlogic.gdx.Files
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.fredboy.cavedroid.common.CaveDroidConstants.PreferenceKeys
import ru.fredboy.cavedroid.common.CaveDroidConstants.SUPPORTED_LOCALES
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.CloudStatsSync
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.NoOpAdController
import ru.fredboy.cavedroid.common.api.NoOpCloudStatsSync
import ru.fredboy.cavedroid.common.api.NoOpInlineTextInput
import ru.fredboy.cavedroid.common.api.NoOpSoftKeyboardObserver
import ru.fredboy.cavedroid.common.api.PreferencesStore
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainThread
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.utils.DEFAULT_VIEWPORT_WIDTH
import ru.fredboy.cavedroid.common.utils.ratio
import ru.fredboy.cavedroid.data.configuration.model.ApplicationContext
import ru.fredboy.cavedroid.domain.configuration.model.LightingBackend
import ru.fredboy.cavedroid.game.world.lighting.LightingSystemFactory
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent
import ru.fredboy.cavedroid.gdx.di.DaggerApplicationComponent
import ru.fredboy.cavedroid.gdx.game.DeathScreen
import ru.fredboy.cavedroid.gdx.game.GameScreen
import ru.fredboy.cavedroid.gdx.menu.v2.PauseMenuScreen
import java.util.Locale

class CaveDroidApplication(
    private val gameDataDirectoryPath: String,
    private val gameDataFileType: Files.FileType,
    private val isTouchScreen: Boolean,
    private val isDebug: Boolean,
    private val preferencesStore: PreferencesStore,
    private val lightingSystemFactory: LightingSystemFactory,
    private val dispatchers: AppDispatchers,
    private val adController: AdController = NoOpAdController(),
    private val cloudStatsSync: CloudStatsSync = NoOpCloudStatsSync(),
    private val inlineTextInput: InlineTextInput = NoOpInlineTextInput,
    private val softKeyboardObserver: SoftKeyboardObserver = NoOpSoftKeyboardObserver,
    private val defaultLocaleProvider: () -> Locale? = { safeDefaultLocale() },
    private val isYandexGamesBuild: Boolean = false,
    loggingSeverity: Severity = Severity.Info,
) : Game(),
    CaveDroidApplicationDecorator {

    init {
        Logger.setMinSeverity(loggingSeverity)
    }

    override lateinit var applicationComponent: ApplicationComponent
        private set

    val applicationComponentOrNull: ApplicationComponent?
        get() = if (::applicationComponent.isInitialized) applicationComponent else null

    var applicationControllerOverride: ApplicationController? = null

    private val applicationScope = CoroutineScope(SupervisorJob() + dispatchers.io)

    private fun initFullscreenMode(isFullscreen: Boolean) {
        if (Gdx.app.type != Application.ApplicationType.Desktop) {
            return
        }

        if (isFullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            Gdx.graphics.setWindowedMode(
                preferencesStore.getPreference(PreferenceKeys.WINDOW_WIDTH_KEY)?.toIntOrNull() ?: 960,
                preferencesStore.getPreference(PreferenceKeys.WINDOW_HEIGHT_KEY)?.toIntOrNull() ?: 540,
            )
        }
    }

    override fun create() {
        GdxMainThread.init()
        val width = DEFAULT_VIEWPORT_WIDTH
        val height = width / Gdx.graphics.ratio

        val isFullscreen = preferencesStore.getPreference(PreferenceKeys.FULLSCREEN).toBoolean()
        initFullscreenMode(isFullscreen)

        val personalizedAdsConsent = preferencesStore.getPreference(PreferenceKeys.PERSONALIZED_ADS_CONSENT)
            ?.toBooleanStrictOrNull()

        applicationComponent = DaggerApplicationComponent.builder()
            .cloudStatsSync(cloudStatsSync)
            .applicationContext(
                ApplicationContext(
                    isDebug = isDebug,
                    isTouch = isTouchScreen,
                    gameDirectory = gameDataDirectoryPath,
                    gameDirectoryFileType = gameDataFileType,
                    width = width,
                    height = height,
                    isFullscreen = isFullscreen,
                    useDynamicCamera = preferencesStore.getPreference(PreferenceKeys.DYNAMIC_CAMERA)
                        ?.toBooleanStrictOrNull() ?: false,
                    isAutoJumpEnabled = preferencesStore.getPreference(PreferenceKeys.AUTO_JUMP)
                        ?.toBooleanStrictOrNull() ?: true,
                    locale = preferencesStore.getPreference(PreferenceKeys.LOCALE)
                        ?.let(::Locale) ?: defaultLocaleProvider()
                        ?.takeIf { it in SUPPORTED_LOCALES } ?: Locale.ENGLISH,
                    soundEnabled = preferencesStore.getPreference(PreferenceKeys.SOUND_ENABLED)
                        ?.toBooleanStrictOrNull() ?: true,
                    isOnboardingShown = preferencesStore.getPreference(PreferenceKeys.ONBOARDING_SHOWN)
                        ?.toBooleanStrictOrNull() ?: false,
                    isInventoryHintShown = preferencesStore.getPreference(PreferenceKeys.INVENTORY_HINT_SHOWN)
                        ?.toBooleanStrictOrNull() ?: false,
                    personalizedAdsConsent = personalizedAdsConsent,
                    lightingBackend = LightingBackend.fromName(
                        preferencesStore.getPreference(PreferenceKeys.LIGHTING_BACKEND),
                    ),
                    isYandexGamesBuild = isYandexGamesBuild,
                ),
            )
            .applicationController(applicationControllerOverride ?: this)
            .preferencesStore(preferencesStore)
            .adController(adController)
            .inlineTextInput(inlineTextInput)
            .softKeyboardObserver(softKeyboardObserver)
            .lightingSystemFactory(lightingSystemFactory)
            .appDispatchers(dispatchers)
            .build()

        if (personalizedAdsConsent != null) {
            adController.setPersonalizedAdsEnabled(personalizedAdsConsent)
        }

        if (gameDataFileType == Files.FileType.Absolute) {
            Gdx.files.absolute(gameDataDirectoryPath).mkdirs()
        }
        applicationComponent.initializeAssets()

        applicationScope.launch {
            applicationComponent.statsRepository.load()
            val remote = runCatching { cloudStatsSync.loadStats() }
                .onFailure { logger.w(it) { "Cloud stats load failed" } }
                .getOrNull()
            if (remote != null) {
                applicationComponent.statsRepository.mergeFromCloud(remote)
                applicationComponent.statsRepository.save()
            }
        }

        setScreen(applicationComponent.menuScreen)
    }

    override fun dispose() {
        runCatching {
            runBlocking { applicationComponent.statsRepository.save() }
        }.onFailure { logger.w(it) { "Stats save on dispose failed" } }
        applicationComponent.menuScreen.dispose()
        applicationComponent.pauseMenuScreen.dispose()
        applicationComponent.gameScreen.dispose()
        applicationComponent.menuSkin.dispose()
        applicationComponent.disposeAssets()
    }

    override fun quitGame() {
        applicationComponent.gameScreen.saveGame()
        adController.showInterstitial {
            Gdx.app.postRunnable {
                applicationComponent.gameScreen.dispose()
                setScreen(applicationComponent.menuScreen)
            }
        }
    }

    override fun startGame(startGameConfig: StartGameConfig) {
        adController.loadInterstitial()
        val gameScreen = applicationComponent.gameScreen.apply {
            when (startGameConfig) {
                is StartGameConfig.New -> newGame(startGameConfig)
                is StartGameConfig.Load -> loadGame(startGameConfig)
            }
        }

        setScreen(gameScreen)
    }

    override fun exitGame() {
        setScreen(null)
        Gdx.app.exit()
    }

    override fun triggerResize() {
        resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    override fun pauseGame() {
        if (screen !is GameScreen) {
            logger.w { "Cannot pause when active screen is not game" }
            return
        }
        screen.pause()
        setScreen(applicationComponent.pauseMenuScreen)
    }

    override fun resumeGame() {
        if (screen !is PauseMenuScreen) {
            logger.w { "Cannot resume when active screen is not pause menu" }
            return
        }
        setScreen(applicationComponent.gameScreen)
        screen.resume()
    }

    override fun saveGame() {
        val gameScreen = when (val currentScreen = screen) {
            is GameScreen -> currentScreen
            is PauseMenuScreen -> applicationComponent.gameScreen
            else -> {
                logger.w { "Cannot save when no game session is active" }
                return
            }
        }
        gameScreen.saveGame()
        resumeGame()
    }

    override fun showDeathScreen() {
        if (screen !is GameScreen) {
            logger.w { "Cannot show death screen when active screen is not game" }
            return
        }
        val deathScreen = applicationComponent.gameScreen.gameComponent?.deathScreen
            ?: run {
                logger.w { "No game component – cannot show death screen" }
                return
            }
        setScreen(deathScreen)
        adController.showBanner()
    }

    override fun respawnPlayer() {
        val gameScreen = applicationComponent.gameScreen
        if (screen !is DeathScreen) {
            logger.w { "Cannot respawn when active screen is not death screen" }
            return
        }
        val wasSoundEnabled = applicationComponent.applicationContextRepository.isSoundEnabled()
        applicationComponent.applicationContextRepository.setSoundEnabled(false)
        applicationComponent.soundPlayer.pauseAll()
        adController.showInterstitial {
            Gdx.app.postRunnable {
                applicationComponent.applicationContextRepository.setSoundEnabled(wasSoundEnabled)
                applicationComponent.soundPlayer.resumeAll()
                gameScreen.respawnPlayer()
                setScreen(gameScreen)
            }
        }
    }

    override fun setScreen(screen: Screen?) {
        try {
            screen?.show()
            screen?.resize(Gdx.graphics.width, Gdx.graphics.height)
        } catch (e: Exception) {
            throw e
        }

        if (screen is GameScreen) {
            applicationComponent.soundPlayer.resumeAll()
        } else {
            applicationComponent.soundPlayer.pauseAll()
        }

        this.screen?.hide()
        this.screen = screen
    }

    fun getPreferencesStore() = preferencesStore

    override fun getDelegate() = this

    companion object {
        private const val TAG = "CaveDroidApplication"
        private val logger = Logger.withTag(TAG)

        private fun safeDefaultLocale(): Locale {
            return try {
                Locale.getDefault()
            } catch (_: Throwable) {
                Locale.ENGLISH
            }
        }
    }
}
