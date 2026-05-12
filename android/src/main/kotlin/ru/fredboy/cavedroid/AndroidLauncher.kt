package ru.fredboy.cavedroid

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import co.touchlab.kermit.Severity
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import ru.fredboy.cavedroid.common.api.AdController
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

        initialize(
            /* listener = */
            CaveDroidApplication(
                gameDataDirectoryPath = gameDataDirectoryPath,
                isTouchScreen = true,
                isDebug = BuildConfig.DEBUG,
                preferencesStore = AndroidPreferencesStore(applicationContext),
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
