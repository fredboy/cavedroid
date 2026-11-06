package ru.fredboy.cavedroid.game.window.waystone

import ru.fredboy.cavedroid.game.window.GameWindowModel

data class WaystoneDestination(val id: String, val name: String, val x: Int, val y: Int)

/** Data and commands supplied by the waystone feature; contains no Scene2D or platform state. */
class WaystoneWindowModel(
    source: WaystoneDestination,
    destinations: List<WaystoneDestination> = emptyList(),
    private val saveName: (String) -> Unit = {},
    private val travel: ((WaystoneDestination) -> Unit)? = null,
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

    val selected: WaystoneDestination? get() = destinations.firstOrNull { it.id == selectedId }
    val canTravel: Boolean get() = selected != null && travel != null

    init {
        updateDestinations(destinations)
        selectedId = this.destinations.firstOrNull()?.id
    }

    fun updateDestinations(values: List<WaystoneDestination>) {
        destinations = values.filter { it.id != source.id }.distinctBy { it.id }
        if (destinations.none { it.id == selectedId }) selectedId = null
        revision++
    }

    fun select(id: String) {
        if (destinations.none { it.id == id } || selectedId == id) return
        selectedId = id
        revision++
    }

    fun rename(value: String): Boolean {
        val name = value.trim()
        if (name.isEmpty() || name.length > MAX_NAME_LENGTH || name.any { it.isISOControl() }) return false
        saveName(name)
        source = source.copy(name = name)
        revision++
        return true
    }

    fun teleport() {
        val destination = selected ?: return
        travel?.invoke(destination)
    }

    companion object {
        const val KEY = "waystone"
        const val MAX_NAME_LENGTH = 32
    }
}
