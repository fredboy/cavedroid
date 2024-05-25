package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssetLoader @Inject constructor(
    private val mainConfig: MainConfig,
) {

    fun getAssetHandle(path: String): FileHandle {
        val texturePackPath =
            mainConfig.assetsPackPath?.let { if (!it.endsWith(File.separator)) "$it${File.separator}" else it }

        return if (texturePackPath == null) {
            Gdx.files.internal(path)
        } else {
            Gdx.files.absolute("$texturePackPath$path")
        }
    }

    fun getGameRendererWidth(): Float {
        return mainConfig.width
    }

    fun getGameRendererHeight(): Float {
        return mainConfig.height
    }

}
