package ru.fredboy.cavedroid.ios

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.foundation.NSFileManager
import org.robovm.apple.foundation.NSSearchPathDirectory
import org.robovm.apple.foundation.NSSearchPathDomainMask
import org.robovm.apple.uikit.UIApplication
import ru.fredboy.cavedroid.gdx.CaveDroidApplication

object IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication? {
        val config = IOSApplicationConfiguration()

        val paths = NSFileManager.getDefaultManager()
            .getURLsForDirectory(
                NSSearchPathDirectory.DocumentDirectory,
                NSSearchPathDomainMask.UserDomainMask,
            )

        val dataDir = paths.firstOrNull()?.path ?: run {
            Gdx.app.error("IOSLauncher", "Couldn't locate data dir")
            Gdx.app.exit()
            return null
        }

        val caveDroidApplication = CaveDroidApplication(
            gameDataDirectoryPath = dataDir,
            isTouchScreen = true,
            isDebug = false,
            preferencesStore = IOSPreferencesStore(),
        )

        return IOSApplication(caveDroidApplication, config)
    }

    @JvmStatic
    fun main(argv: Array<String>) {
        val pool = NSAutoreleasePool()
        UIApplication.main<UIApplication?, IOSLauncher?>(argv, null, IOSLauncher::class.java)
        pool.close()
    }
}
