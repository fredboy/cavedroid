package ru.fredboy.cavedroid

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import co.touchlab.kermit.Severity
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

class AndroidLauncher : AndroidApplication() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameDataDirectoryPath = packageManager.getPackageInfo(packageName, 0)
            .applicationInfo?.dataDir ?: run {
            finish()
            return
        }

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
                loggingSeverity = if (BuildConfig.DEBUG) Severity.Debug else Severity.Info,
            ),
            /* config = */ config,
        )
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        // ignore
    }
}
