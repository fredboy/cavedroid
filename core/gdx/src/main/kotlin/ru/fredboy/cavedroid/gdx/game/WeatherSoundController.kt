package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.model.RainSound
import ru.fredboy.cavedroid.domain.assets.repository.WeatherSoundAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.WeatherLightning
import javax.inject.Inject

@GameScope
class WeatherSoundController @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val weatherSoundAssetsRepository: WeatherSoundAssetsRepository,
    private val soundPlayer: SoundPlayer,
    private val applicationContextRepository: ApplicationContextRepository,
    private val lightning: WeatherLightning,
) : Disposable {

    private var rainSound: RainSound? = null
    private var rainSoundId: Long = -1L
    private var playbackSeconds = 0.0

    fun update(delta: Float) {
        val player = mobController.player
        val biomeFactor = gameWorld.biomeProximityFactor(
            centerX = player.position.x,
            rangeBlocks = BIOME_FADE_BLOCKS,
        ) { it == Biome.PLAINS }
        val depthFactor = depthFactor(player.middleMapY)
        val intensity = gameWorld.weatherIntensity * biomeFactor * depthFactor

        if (intensity > 0f) {
            playbackSeconds += delta.toDouble()
            if (applicationContextRepository.isSoundEnabled()) {
                playRain(intensity)
            } else {
                stopRainSound()
                // Weather remains visible when sound is muted.
                loadRain()
            }
            lightning.intensity = (rainSound?.thunder?.flashAt(playbackSeconds) ?: 0f) * intensity
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
        val sound = loadRain()?.sound ?: return
        if (rainSoundId == -1L) {
            rainSoundId = soundPlayer.playLoopSound(sound, volume)
            // Each new loop starts at sample zero, including after unmuting.
            playbackSeconds = 0.0
        } else {
            sound.setVolume(rainSoundId, volume)
        }
    }

    private fun loadRain(): RainSound? = rainSound ?: weatherSoundAssetsRepository.getRainSound()?.also { rainSound = it }

    private fun stopRainSound() {
        rainSound?.sound?.takeIf { rainSoundId != -1L }?.let { soundPlayer.stopLoopSound(it, rainSoundId) }
        rainSoundId = -1L
    }

    private fun stopRain() {
        stopRainSound()
        playbackSeconds = 0.0
        lightning.intensity = 0f
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
