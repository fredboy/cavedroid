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
            dismissed = true
            applicationContextRepository.setInventoryHintShown(true)
        }
    }

    override fun notifyItemMoved() {
        if (dismissed || fadeElapsedSec >= 0f) return
        fadeElapsedSec = 0f
    }

    override fun notifyItemHeld() {
        // TODO
    }

    companion object {
        private const val FADE_DURATION_SEC = 2f
    }
}
