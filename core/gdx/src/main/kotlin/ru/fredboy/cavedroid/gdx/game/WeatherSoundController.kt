package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class WeatherSoundController @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val weatherSoundAssetsRepository: WeatherSoundAssetsRepository,
) : Disposable {

    private var rainSound: Sound? = null
    private var rainSoundId: Long = -1L

    fun update() {
        val inRainBiome = gameWorld.getBiomeAt(mobController.player.mapX) == Biome.PLAINS
        val intensity = if (inRainBiome) gameWorld.weatherIntensity else 0f

        if (intensity > 0f) {
            playRain(intensity)
        } else {
            stopRain()
        }
    }

    private fun playRain(intensity: Float) {
        val volume = intensity * MAX_VOLUME
        val sound = rainSound ?: weatherSoundAssetsRepository.getRainSound()?.also { rainSound = it } ?: return
        if (rainSoundId == -1L) {
            rainSoundId = sound.loop(volume)
        } else {
            sound.setVolume(rainSoundId, volume)
        }
    }

    private fun stopRain() {
        rainSound?.takeIf { rainSoundId != -1L }?.stop(rainSoundId)
        rainSoundId = -1L
    }

    override fun dispose() {
        stopRain()
        rainSound = null
    }

    companion object {
        private const val MAX_VOLUME = 0.5f
    }
}
