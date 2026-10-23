package ru.fredboy.cavedroid

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.window.OnBackInvokedDispatcher
import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import kotlinx.coroutines.Dispatchers
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.common.performance.PerformanceOperation
import ru.fredboy.cavedroid.common.performance.PerformanceStartKind
import ru.fredboy.cavedroid.gameplay.lighting.bfs.BfsLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.performance.AndroidPerformanceMonitor
import java.util.concurrent.atomic.AtomicBoolean

class AndroidLauncher : AndroidApplication() {

    private lateinit var performanceMonitor: AndroidPerformanceMonitor
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {}
        override fun onDisplayRemoved(displayId: Int) {}
        override fun onDisplayChanged(displayId: Int) {
            updateRefreshRate()
        }
    }

    private fun updateRefreshRate() {
        if (!::performanceMonitor.isInitialized) return
        val hz = windowManager.defaultDisplay.mode.refreshRate.toDouble()
        Gdx.app?.postRunnable { performanceMonitor.setRefreshRate(hz) }
    }

    private lateinit var adController: AdController
    private lateinit var saveTransferController: AndroidSaveTransferController

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferencesStore = AndroidPreferencesStore(applicationContext)
        performanceMonitor = AndroidPerformanceMonitor(this, preferencesStore)
        performanceMonitor.startAndroidStartup(
            if (processStarted.compareAndSet(false, true)) {
                PerformanceStartKind.COLD_PROCESS
            } else {
                PerformanceStartKind.ACTIVITY_RECREATION
            },
        )
        super.onCreate(savedInstanceState)

        val gameDataDirectoryPath = packageManager.getPackageInfo(packageName, 0)
            .applicationInfo?.dataDir ?: run {
            finish()
            return
        }

        adController = createAdController(this)
        saveTransferController = AndroidSaveTransferController(this)

        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_OVERLAY) {
            }
        }

        (getSystemService(DISPLAY_SERVICE) as DisplayManager).registerDisplayListener(displayListener, null)
        val softKeyboardObserver = AndroidSoftKeyboardObserver(this)

        initialize(
            /* listener = */
            CaveDroidApplication(
                gameDataDirectoryPath = gameDataDirectoryPath,
                gameDataFileType = Files.FileType.Absolute,
                isTouchScreen = true,
                isDebug = BuildConfig.DEBUG,
                preferencesStore = preferencesStore,
                performanceMonitor = performanceMonitor,
                lightingSystemFactory = BfsLightingSystemFactory(),
                dispatchers = AppDispatchers(
                    io = Dispatchers.IO,
                    background = Dispatchers.Default,
                    main = GdxMainDispatcher,
                ),
                adController = adController,
                softKeyboardObserver = softKeyboardObserver,
                saveTransferController = saveTransferController,
                loggingSeverity = if (BuildConfig.DEBUG) Severity.Debug else Severity.Info,
            ),
            /* config = */ config,
        )
    }

    private var resumedBefore = false

    override fun onResume() {
        val resume = if (resumedBefore && ::performanceMonitor.isInitialized) {
            performanceMonitor.beginOperation(PerformanceOperation.ANDROID_STARTUP, startKind = PerformanceStartKind.FOREGROUND_RESUME)
        } else {
            null
        }
        resumedBefore = true
        super.onResume()
        updateRefreshRate()
        pinScreenBrightness()
        adController.resume()
        Gdx.app?.postRunnable { resume?.finish() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (saveTransferController.handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun pinScreenBrightness() {
        val systemBrightness = try {
            Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (_: Settings.SettingNotFoundException) {
            return
        }
        val normalized = (systemBrightness / 255f).coerceIn(0f, 1f)
        window.attributes = window.attributes.apply { screenBrightness = normalized }
    }

    override fun onPause() {
        super.onPause()
        adController.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adController.destroy()
        (getSystemService(DISPLAY_SERVICE) as DisplayManager).unregisterDisplayListener(displayListener)
    }

    private companion object {
        val processStarted = AtomicBoolean()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        // ignore
    }
}
