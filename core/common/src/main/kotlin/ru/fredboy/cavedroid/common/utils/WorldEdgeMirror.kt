package ru.fredboy.cavedroid.common.utils

/**
 * Clamps a requested edge-mirror band so a single chunk is never asked to mirror
 * itself onto both sides of the world (which would happen when the band exceeds
 * half the world width).
 */
fun effectiveMirrorBand(requestedBand: Int, worldWidth: Int): Int {
    require(requestedBand >= 0) { "requestedBand must be >= 0, was $requestedBand" }
    require(worldWidth >= 0) { "worldWidth must be >= 0, was $worldWidth" }
    return minOf(requestedBand, worldWidth / 2)
}

/**
 * Returns the side signs (+1 right of origin, -1 left of origin) for which the
 * given chunk position needs an edge mirror. Empty if the chunk is far from both
 * edges.
 *
 * Intended for use with an already-clamped [band] from [effectiveMirrorBand].
 */
fun mirrorSidesFor(chunkPosition: Int, worldWidth: Int, band: Int): List<Int> {
    if (band <= 0 || worldWidth <= 0) return emptyList()
    val sides = mutableListOf<Int>()
    if (chunkPosition < band) sides.add(1)
    if (chunkPosition >= worldWidth - band) sides.add(-1)
    return sides
}
