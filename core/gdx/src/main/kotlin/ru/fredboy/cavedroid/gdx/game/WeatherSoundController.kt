package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.utils.Disposable
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
    private val applicationContextRepository: ApplicationContextRepository,
    private val lightning: WeatherLightning,
) : Disposable {

    private var rainSound: RainSound? = null
    private var playing = false
    private var previousPosition = 0.0
    private var loopOffset = 0.0
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
            if (applicationContextRepository.isSoundEnabled()) {
                playRain(intensity)
            } else {
                stopRainSound()
                playbackSeconds += delta.toDouble()
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
        val rain = loadRain() ?: return
        val music = rain.music
        music.volume = intensity * MAX_VOLUME
        if (!playing) {
            music.isLooping = true
            music.play()
            playing = true
        }
        // Use the decoder clock: simulation deltas drift and Android may buffer playback.
        val position = music.position.toDouble()
        if (position < previousPosition) loopOffset += rain.thunder?.durationSeconds ?: 0.0
        previousPosition = position
        playbackSeconds = loopOffset + position
    }

    fun pause() {
        if (playing) rainSound?.music?.pause()
        playing = false
    }

    private fun loadRain(): RainSound? = rainSound ?: weatherSoundAssetsRepository.getRainSound()?.also { rainSound = it }

    private fun stopRainSound() {
        if (playing || previousPosition > 0.0) rainSound?.music?.stop()
        playing = false
        previousPosition = 0.0
        loopOffset = 0.0
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
