package ru.fredboy.cavedroid.domain.world.waystone

import ru.fredboy.cavedroid.domain.world.model.Layer

data class Waystone(val x: Int, val y: Int, val layer: Layer, val name: String) {
    val id: String get() = "${layer.z}:$x:$y"
}

enum class WaystoneTravelResult {
    SUCCESS,
    SOURCE_UNAVAILABLE,
    DESTINATION_UNAVAILABLE,
    DESTINATION_BLOCKED,
    INTERRUPTED,
}

interface WaystoneTravel {
    val playerHealth: Int
    fun activate(x: Int, y: Int, defaultName: String): Waystone?
    fun destinations(sourceId: String): List<Waystone>
    fun rename(id: String, name: String): Boolean
    fun canUse(sourceId: String): Boolean
    fun distance(source: Waystone, destination: Waystone): Long
    fun teleport(sourceId: String, destinationId: String): WaystoneTravelResult
}
