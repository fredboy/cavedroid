package ru.fredboy.cavedroid.data.configuration.repository

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.store.GameContextStore
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameContextRepositoryImpl @Inject constructor(
    private val gameContextStore: GameContextStore
) : GameContextRepository {

    override fun isTouch(): Boolean = gameContextStore.isTouch

    override fun getGameDirectory(): String = gameContextStore.gameDirectory

    override fun getWidth(): Float = gameContextStore.width

    override fun getHeight(): Float = gameContextStore.height

    override fun shouldShowInfo(): Boolean = gameContextStore.showInfo

    override fun shouldShowMap(): Boolean = gameContextStore.showMap

    override fun getJoystick(): Joystick? = gameContextStore.joystick

    override fun isFullscreen(): Boolean = gameContextStore.isFullscreen

    override fun useDynamicCamera(): Boolean = gameContextStore.useDynamicCamera

    override fun getCameraContext(): CameraContext? = gameContextStore.cameraContext

    override fun setTouch(isTouch: Boolean) {
        gameContextStore.isTouch = isTouch
    }

    override fun setGameDirectory(path: String) {
        gameContextStore.gameDirectory = path
    }

    override fun setWidth(width: Float) {
        gameContextStore.width = width
    }

    override fun setHeight(height: Float) {
        gameContextStore.height = height
    }

    override fun setShowInfo(show: Boolean) {
        gameContextStore.showInfo = show
    }

    override fun setShowMap(show: Boolean) {
        gameContextStore.showMap = show
    }

    override fun setJoystick(joystick: Joystick?) {
        gameContextStore.joystick = joystick
    }

    override fun setFullscreen(fullscreen: Boolean) {
        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode);
        } else {
            Gdx.graphics.setWindowedMode(getWidth().toInt(), getHeight().toInt());
        }
        gameContextStore.isFullscreen = fullscreen
    }

    override fun setUseDynamicCamera(use: Boolean) {
        gameContextStore.useDynamicCamera = use
    }

    override fun setCameraContext(cameraContext: CameraContext?) {
        gameContextStore.cameraContext = cameraContext
    }
}
