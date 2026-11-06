package ru.fredboy.cavedroid.game.window.waystone

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import javax.inject.Inject

/** Waystone-specific UI entry point; the shared manager and host do not depend on this feature. */
@GameScope
class WaystoneWindowController @Inject constructor(
    private val windows: GameWindowsManager,
    private val items: ItemsRepository,
) {
    private val sessionNames = mutableMapOf<Pair<Int, Int>, String>()

    fun open(x: Int, y: Int) {
        val position = x to y
        val name = sessionNames[position] ?: items.getItemByKey("waystone").params.name
        windows.openWindow(
            WaystoneWindowModel(
                source = WaystoneDestination("$x:$y", name, x, y),
                saveName = { sessionNames[position] = it },
            ),
        )
    }
}
