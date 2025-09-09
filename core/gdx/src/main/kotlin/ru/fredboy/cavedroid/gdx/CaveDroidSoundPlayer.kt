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

        val pan = MathUtils.clamp(dx / maxHearingDistance, -1f, 1f)

        sound.play(volume, pitch, pan)
    }

    override fun playUiSound(sound: Sound) {
        if (!applicationContextRepository.isSoundEnabled()) {
            return
        }

        sound.play(0.3f, 1f, 0f)
    }

    companion object {
        private const val HEARING_DISTANCE = 16f
    }
}
