package ru.deadsoftware.cavedroid.game.ui

import com.badlogic.gdx.utils.Timer
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import javax.inject.Inject

@GameScope
class TooltipManager @Inject constructor(
    private val mainConfig: MainConfig
) {

    private val resetTask = object : Timer.Task() {
        override fun run() {
            currentHotbarTooltip = ""
        }
    }

    var currentHotbarTooltip: String = ""
        private set

    var currentMouseTooltip: String = ""
        private set

    fun showHotbarTooltip(tooltip: String) {
        currentHotbarTooltip = tooltip
        if (resetTask.isScheduled) {
            resetTask.cancel()
        }
        Timer.schedule(resetTask, TOOLTIP_TIME_S)
    }

    fun showMouseTooltip(tooltip: String) {
        currentMouseTooltip = tooltip
    }

    companion object {
        private const val TOOLTIP_TIME_S = 2f
    }

}