package ru.fredboy.cavedroid.data.configuration.repository

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.model.Joystick
import ru.fredboy.cavedroid.data.configuration.store.GameContextStore
import ru.fredboy.cavedroid.domain.configuration.model.CameraContext
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import javax.inject.Inject

@GameScope
class GameContextRepositoryImpl @Inject constructor(
    private val gameContextStore: GameContextStore,
) : GameContextRepository {

    override fun isLoadGame(): Boolean = gameContextStore.isLoadGame

    override fun shouldShowInfo(): Boolean = gameContextStore.showInfo

    override fun shouldShowMap(): Boolean = gameContextStore.showMap

    override fun getJoystick(): Joystick = gameContextStore.joystick

    override fun getCameraContext(): CameraContext = gameContextStore.cameraContext

    override fun setShowInfo(show: Boolean) {
        gameContextStore.showInfo = show
    }

    override fun setShowMap(show: Boolean) {
        gameContextStore.showMap = show
    }

    override fun setJoystick(joystick: Joystick) {
        gameContextStore.joystick = joystick
    }

    override fun setCameraContext(cameraContext: CameraContext) {
        gameContextStore.cameraContext = cameraContext
    }
}
