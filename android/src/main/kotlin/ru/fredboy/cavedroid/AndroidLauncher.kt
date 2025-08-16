package ru.fredboy.cavedroid

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

class AndroidLauncher : AndroidApplication() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameDataDirectoryPath = packageManager.getPackageInfo(packageName, 0)
            .applicationInfo.dataDir

        val config = AndroidApplicationConfiguration()
        config.useImmersiveMode = true

        initialize(
            /* listener = */ CaveDroidApplication(
                gameDataDirectoryPath = gameDataDirectoryPath,
                isTouchScreen = true,
                isDebug = BuildConfig.DEBUG,
                preferencesStore = AndroidPreferencesStore(applicationContext),
            ),
            /* config = */ config,
        )
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        // ignore
    }
}
