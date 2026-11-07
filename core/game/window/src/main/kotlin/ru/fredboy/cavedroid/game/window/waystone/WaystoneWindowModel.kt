package ru.fredboy.cavedroid.game.window.waystone

import ru.fredboy.cavedroid.domain.world.waystone.WaystoneTravelResult
import ru.fredboy.cavedroid.game.window.GameWindowModel

data class WaystoneDestination(val id: String, val name: String, val x: Int, val y: Int, val distance: Long? = null)

/** Data and commands supplied by the waystone feature; contains no Scene2D or platform state. */
class WaystoneWindowModel(
    source: WaystoneDestination,
    destinations: List<WaystoneDestination> = emptyList(),
    private val saveName: (String) -> Boolean = { true },
    private val travel: ((WaystoneDestination) -> WaystoneTravelResult)? = null,
    private val refreshDestinations: (() -> List<WaystoneDestination>)? = null,
    private val canStart: () -> Boolean = { true },
    private val onChargeStarted: () -> Unit = {},
    private val canContinue: () -> Boolean = { true },
) : GameWindowModel {
    override val key = KEY

    var source = source
        private set

    var destinations: List<WaystoneDestination> = emptyList()
        private set

    var selectedId: String? = null
        private set

    var revision = 0
        private set

    private var chargeDestination: String? = null
    private var remaining = 0f
    private var closed = false
    val isCharging get() = chargeDestination != null
    val chargeProgress get() = if (isCharging) 1f - remaining / CHARGE_DURATION else 0f
    var completed = false
        private set
    var failure: WaystoneTravelResult? = null
        private set

    val selected: WaystoneDestination? get() = destinations.firstOrNull { it.id == selectedId }
    val canTravel: Boolean get() = !closed && !completed && !isCharging && selected != null && travel != null

    init {
        updateDestinations(destinations)
        selectedId = this.destinations.firstOrNull()?.id
    }

    fun updateDestinations(values: List<WaystoneDestination>) {
        destinations = values.filter { it.id != source.id }.distinctBy { it.id }
        if (destinations.none { it.id == selectedId }) selectedId = null
        if (isCharging && destinations.none { it.id == chargeDestination }) cancelCharge(WaystoneTravelResult.DESTINATION_UNAVAILABLE)
        revision++
    }

    fun select(id: String) {
        if (closed || isCharging || destinations.none { it.id == id } || selectedId == id) return
        selectedId = id
        revision++
    }

    fun rename(value: String): Boolean {
        val name = value.trim()
        if (closed || isCharging || name.isEmpty() || name.length > MAX_NAME_LENGTH || name.any { it.isISOControl() }) return false
        if (!saveName(name)) {
            failure = WaystoneTravelResult.SOURCE_UNAVAILABLE
            revision++
            return false
        }
        source = source.copy(name = name)
        revision++
        return true
    }

    fun teleport() {
        if (!canTravel) return
        failure = null
        if (!canStart()) {
            failure = WaystoneTravelResult.SOURCE_UNAVAILABLE
        } else {
            onChargeStarted()
            chargeDestination = selectedId
            remaining = CHARGE_DURATION
        }
        revision++
    }

    fun update(delta: Float) {
        if (closed || completed) return
        refreshDestinations?.invoke()?.let { if (it != destinations) updateDestinations(it) }
        val destinationId = chargeDestination ?: return
        if (!canContinue()) {
            cancelCharge(WaystoneTravelResult.INTERRUPTED)
            return
        }
        if (delta.isFinite()) remaining -= delta.coerceAtLeast(0f)
        if (remaining > 0f) return
        val destination = destinations.firstOrNull { it.id == destinationId }
        chargeDestination = null
        val result = destination?.let { travel?.invoke(it) } ?: WaystoneTravelResult.DESTINATION_UNAVAILABLE
        completed = result == WaystoneTravelResult.SUCCESS
        failure = result.takeUnless { completed }
        revision++
    }

    fun cancelCharge(reason: WaystoneTravelResult? = null) {
        chargeDestination = null
        remaining = 0f
        failure = reason
        revision++
    }

    fun dismissFailure() {
        failure = null
        revision++
    }

    override fun onClose() {
        closed = true
        cancelCharge()
    }

    companion object {
        const val KEY = "waystone"
        const val MAX_NAME_LENGTH = 32
        const val CHARGE_DURATION = 1f
    }
}
