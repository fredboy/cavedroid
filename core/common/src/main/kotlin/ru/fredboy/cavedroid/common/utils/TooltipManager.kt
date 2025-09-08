package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.utils.Timer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TooltipManager @Inject constructor() {

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
