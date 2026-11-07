package ru.fredboy.cavedroid.game.window.waystone

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.waystone.Waystone
import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravel
import ru.fredboy.cavedroid.game.window.GameWindowsManager
import javax.inject.Inject

/** Waystone-specific UI entry point; the shared manager and host do not depend on this feature. */
@GameScope
class WaystoneWindowController @Inject constructor(
    private val windows: GameWindowsManager,
    private val items: ItemsRepository,
    private val travel: WaystoneTravel,
) {
    fun open(x: Int, y: Int): Boolean {
        val source = travel.activate(x, y, items.getItemByKey("waystone").params.name) ?: return false
        fun destinations() = travel.destinations(source.id).map { it.toDestination(travel.distance(source, it)) }
        var chargeHealth = 0
        windows.openWindow(
            WaystoneWindowModel(
                source = source.toDestination(),
                destinations = destinations(),
                saveName = { travel.rename(source.id, it) },
                travel = { travel.teleport(source.id, it.id) },
                refreshDestinations = ::destinations,
                canStart = { travel.canUse(source.id) },
                onChargeStarted = { chargeHealth = travel.playerHealth },
                canContinue = { travel.playerHealth >= chargeHealth && travel.canUse(source.id) },
            ),
        )
        return true
    }

    private fun Waystone.toDestination(distance: Long? = null) = WaystoneDestination(id, name, x, y, distance)
}
