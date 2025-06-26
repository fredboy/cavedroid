package ru.fredboy.cavedroid.data.configuration.repository

import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.store.GameConfigurationStore
import ru.fredboy.cavedroid.domain.configuration.repository.GameConfigurationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameConfigurationRepositoryImpl @Inject constructor(
    private val gameConfigurationStore: GameConfigurationStore
) : GameConfigurationRepository {

    override fun isTouch(): Boolean = gameConfigurationStore.isTouch

    override fun getGameDirectory(): String = gameConfigurationStore.gameDirectory

    override fun getWidth(): Float = gameConfigurationStore.width

    override fun getHeight(): Float = gameConfigurationStore.height

    override fun shouldShowInfo(): Boolean = gameConfigurationStore.showInfo

    override fun shouldShowMap(): Boolean = gameConfigurationStore.showMap

    override fun getJoystick(): Joystick? = gameConfigurationStore.joystick

    override fun isFullscreen(): Boolean = gameConfigurationStore.isFullscreen

    override fun useDynamicCamera(): Boolean = gameConfigurationStore.useDynamicCamera

    override fun setTouch(isTouch: Boolean) {
        gameConfigurationStore.isTouch = isTouch
    }

    override fun setGameDirectory(path: String) {
        gameConfigurationStore.gameDirectory = path
    }

    override fun setWidth(width: Float) {
        gameConfigurationStore.width = width
    }

    override fun setHeight(height: Float) {
        gameConfigurationStore.height = height
    }

    override fun setShowInfo(show: Boolean) {
        gameConfigurationStore.showInfo = show
    }

    override fun setShowMap(show: Boolean) {
        gameConfigurationStore.showMap = show
    }

    override fun setJoystick(joystick: Joystick?) {
        gameConfigurationStore.joystick = joystick
    }

    override fun setFullscreen(fullscreen: Boolean) {
        gameConfigurationStore.isFullscreen = fullscreen
    }

    override fun setUseDynamicCamera(use: Boolean) {
        gameConfigurationStore.useDynamicCamera = use
    }
}
