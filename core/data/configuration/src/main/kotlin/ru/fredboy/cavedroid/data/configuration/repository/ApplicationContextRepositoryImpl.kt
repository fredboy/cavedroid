package ru.fredboy.cavedroid.data.configuration.repository

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.data.configuration.store.ApplicationContextStore
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationContextRepositoryImpl @Inject constructor(
    private val applicationContextStore: ApplicationContextStore,
) : ApplicationContextRepository {

    override fun isDebug(): Boolean = applicationContextStore.isDebug

    override fun isTouch(): Boolean = applicationContextStore.isTouch

    override fun isFullscreen(): Boolean = applicationContextStore.isFullscreen

    override fun useDynamicCamera(): Boolean = applicationContextStore.useDynamicCamera

    override fun getGameDirectory(): String = applicationContextStore.gameDirectory

    override fun getWidth(): Float = applicationContextStore.width

    override fun getHeight(): Float = applicationContextStore.height

    override fun setTouch(isTouch: Boolean) {
        applicationContextStore.isTouch = isTouch
    }

    override fun setFullscreen(fullscreen: Boolean) {
        if (!Gdx.graphics.supportsDisplayModeChange()) {
            return
        }

        if (fullscreen) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            Gdx.graphics.setWindowedMode(960, 540)
            setWidth(Gdx.graphics.width.toFloat() / 2)
            setHeight(Gdx.graphics.height.toFloat() / 2)
        }
        applicationContextStore.isFullscreen = fullscreen
    }

    override fun setUseDynamicCamera(use: Boolean) {
        applicationContextStore.useDynamicCamera = use
    }

    override fun setGameDirectory(path: String) {
        applicationContextStore.gameDirectory = path
    }

    override fun setWidth(width: Float) {
        applicationContextStore.width = width
    }

    override fun setHeight(height: Float) {
        applicationContextStore.height = height
    }
}
