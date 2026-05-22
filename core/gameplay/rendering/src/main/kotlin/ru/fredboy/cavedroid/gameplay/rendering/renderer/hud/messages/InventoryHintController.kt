package ru.fredboy.cavedroid.gameplay.rendering.renderer.hud.messages

import ru.fredboy.cavedroid.common.api.InventoryHintEvents
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import javax.inject.Inject

@GameScope
class InventoryHintController @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
) : InventoryHintEvents {

    private var dismissed: Boolean = applicationContextRepository.isInventoryHintShown()
    private var fadeElapsedSec: Float = -1f

    var hintIndex = 0
        private set

    val isVisible: Boolean
        get() = !dismissed

    val alpha: Float
        get() = when {
            dismissed -> 0f
            fadeElapsedSec < 0f -> 1f
            else -> (1f - fadeElapsedSec / FADE_DURATION_SEC).coerceIn(0f, 1f)
        }

    fun tick(delta: Float) {
        if (dismissed || fadeElapsedSec < 0f) return
        fadeElapsedSec += delta
        if (fadeElapsedSec >= FADE_DURATION_SEC) {
            fadeElapsedSec = -1f
            hintIndex++
            dismissed = hintIndex >= TOTAL_HINTS
            if (dismissed) {
                applicationContextRepository.setInventoryHintShown(true)
            }
        }
    }

    override fun notifyItemMoved() {
        if (hintIndex != 0 || dismissed || fadeElapsedSec >= 0f) return
        fadeElapsedSec = 0f
    }

    override fun notifyItemHeld() {
        if (hintIndex != 1 || dismissed || fadeElapsedSec >= 0f) return
        fadeElapsedSec = 0f
    }

    companion object {
        private const val TOTAL_HINTS = 2
        private const val FADE_DURATION_SEC = 2f
    }
}
