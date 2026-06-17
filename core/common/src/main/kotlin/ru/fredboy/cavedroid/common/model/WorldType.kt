package ru.fredboy.cavedroid.common.model

/**
 * World topology.
 *
 * [LOOPING] is the classic fixed-width, horizontally-wrapping world. [INFINITE] is an
 * unbounded, Minecraft-like world whose chunks are generated lazily as the player explores.
 */
enum class WorldType {
    LOOPING,
    INFINITE,
    ;

    companion object {
        val DEFAULT = LOOPING
    }
}
