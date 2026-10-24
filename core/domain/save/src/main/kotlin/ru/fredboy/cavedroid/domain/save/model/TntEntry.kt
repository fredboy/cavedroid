package ru.fredboy.cavedroid.domain.save.model

/** Position is the center of the primed TNT. Unloaded TNT retains its remaining fuse. */
data class TntEntry(
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float,
    val fuse: Float,
)
