package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class WeatherSoundController @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val weatherSoundAssetsRepository: WeatherSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    private val applicationContextRepository: ApplicationContextRepository,
) : Disposable {

    private var rainSound: Sound? = null
    private var rainSoundId: Long = -1L

    fun update() {
        if (!applicationContextRepository.isSoundEnabled()) {
            stopRain()
            return
        }

        val player = mobController.player
        val biomeFactor = gameWorld.biomeProximityFactor(
            centerX = player.position.x,
            rangeBlocks = BIOME_FADE_BLOCKS,
        ) { it == Biome.PLAINS }
        val depthFactor = depthFactor(player.middleMapY)
        val intensity = gameWorld.weatherIntensity * biomeFactor * depthFactor

        if (intensity > 0f) {
            playRain(intensity)
        } else {
            stopRain()
        }
    }

    private fun depthFactor(playerMapY: Int): Float {
        val depth = playerMapY - gameWorld.generatorConfig.seaLevel
        return (1f - depth.toFloat() / DEPTH_FADE_BLOCKS).coerceIn(0f, 1f)
    }

    private fun playRain(intensity: Float) {
        val volume = intensity * MAX_VOLUME
        val sound = rainSound ?: weatherSoundAssetsRepository.getRainSound()?.also { rainSound = it } ?: return
        if (rainSoundId == -1L) {
            rainSoundId = soundPlayer.playLoopSound(sound, volume)
        } else {
            sound.setVolume(rainSoundId, volume)
        }
    }

    private fun stopRain() {
        rainSound?.takeIf { rainSoundId != -1L }?.let { soundPlayer.stopLoopSound(it, rainSoundId) }
        rainSoundId = -1L
    }

    override fun dispose() {
        stopRain()
        rainSound = null
    }

    companion object {
        private const val MAX_VOLUME = 0.5f
        private const val DEPTH_FADE_BLOCKS = 24f
        private const val BIOME_FADE_BLOCKS = 16f
    }
}
