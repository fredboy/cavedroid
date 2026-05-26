package ru.fredboy.cavedroid

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import co.touchlab.kermit.Severity
import com.badlogic.gdx.Files
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import kotlinx.coroutines.Dispatchers
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.coroutines.AppDispatchers
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.gameplay.lighting.bfs.BfsLightingSystemFactory
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

class AndroidLauncher : AndroidApplication() {

    private lateinit var adController: AdController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameDataDirectoryPath = packageManager.getPackageInfo(packageName, 0)
            .applicationInfo?.dataDir ?: run {
            finish()
            return
        }

        adController = createAdController(this)

        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(Int.MAX_VALUE) {
            }
        }

        val preferencesStore = AndroidPreferencesStore(applicationContext)

        initialize(
            /* listener = */
            CaveDroidApplication(
                gameDataDirectoryPath = gameDataDirectoryPath,
                gameDataFileType = Files.FileType.Absolute,
                isTouchScreen = true,
                isDebug = BuildConfig.DEBUG,
                preferencesStore = preferencesStore,
                lightingSystemFactory = BfsLightingSystemFactory(),
                dispatchers = AppDispatchers(
                    io = Dispatchers.IO,
                    background = Dispatchers.Default,
                    main = GdxMainDispatcher,
                ),
                adController = adController,
                loggingSeverity = if (BuildConfig.DEBUG) Severity.Debug else Severity.Info,
            ),
            /* config = */ config,
        )
    }

    override fun onResume() {
        super.onResume()
        adController.resume()
    }

    override fun onPause() {
        super.onPause()
        adController.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        adController.destroy()
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        // ignore
    }
}
