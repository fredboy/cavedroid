package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.api.SoundPlayer
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class CaveDroidSoundPlayer @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : SoundPlayer {

    // Track active loops so pauseAll/resumeAll only touch sounds we started — Web Audio
    // forbids resume() before a user gesture if nothing was previously playing.
    private val activeLoops = mutableMapOf<Sound, MutableSet<Long>>()
    private var pausedLoops: Map<Sound, Set<Long>> = emptyMap()

    override fun playSoundAtPosition(
        sound: Sound,
        soundX: Float,
        soundY: Float,
        playerX: Float,
        playerY: Float,
        pitch: Float,
    ) {
        if (!applicationContextRepository.isSoundEnabled()) {
            return
        }

        val maxHearingDistance = HEARING_DISTANCE
        val dx = soundX - playerX
        val dy = soundY - playerY
        val distance = MathUtils.clamp(sqrt((dx * dx + dy * dy).toDouble()).toFloat(), 0f, maxHearingDistance)

        val volume = 1.0f - (distance / maxHearingDistance)

        if (volume <= 0f) {
            return
        }
        val pan = MathUtils.clamp(dx / maxHearingDistance, -1f, 1f)

        sound.play(volume, pitch, pan)
    }

    override fun playUiSound(sound: Sound) {
        if (!applicationContextRepository.isSoundEnabled()) {
            return
        }

        sound.play(0.3f, 1f, 0f)
    }

    override fun playLoopSound(sound: Sound, volume: Float): Long {
        if (!applicationContextRepository.isSoundEnabled()) {
            return -1L
        }

        val id = sound.loop(volume)
        activeLoops.getOrPut(sound) { mutableSetOf() } += id
        return id
    }

    override fun stopLoopSound(sound: Sound, id: Long) {
        if (id == -1L) return
        sound.stop(id)
        activeLoops[sound]?.let { ids ->
            ids -= id
            if (ids.isEmpty()) {
                activeLoops -= sound
            }
        }
    }

    override fun pauseAll() {
        val snapshot = activeLoops.mapValues { it.value.toSet() }
        pausedLoops = snapshot
        snapshot.forEach { (sound, ids) ->
            ids.forEach { sound.pause(it) }
        }
    }

    override fun resumeAll() {
        pausedLoops.forEach { (sound, ids) ->
            ids.forEach { sound.resume(it) }
        }
        pausedLoops = emptyMap()
    }

    companion object {
        private const val HEARING_DISTANCE = 16f
    }
}
