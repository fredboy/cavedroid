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

/**
 * True if a block at world-x [x] (assumed in `[0, worldWidth)`) falls into the
 * range `[rangeStart, rangeStart + rangeWidth)` after accounting for horizontal
 * world wrap. The range may straddle or sit entirely outside `[0, worldWidth)`
 * (e.g. for chunk frame buffers rendering the seam-wrapped portion of the
 * viewport).
 */
fun wrapsIntoRange(x: Int, rangeStart: Int, rangeWidth: Int, worldWidth: Int): Boolean {
    return wrapsIntoRange(x, rangeStart, rangeStart + rangeWidth, rangeWidth, worldWidth)
}

/**
 * True if a block at world-x [x] (assumed in `[0, worldWidth)`) falls into the
 * range `[rangeStart, rangeStart + rangeWidth)` after accounting for horizontal
 * world wrap. The range may straddle or sit entirely outside `[0, worldWidth)`
 * (e.g. for chunk frame buffers rendering the seam-wrapped portion of the
 * viewport).
 */
fun wrapsIntoRange(x: Int, rangeStart: Int, rangeEnd: Int, rangeWidth: Int, worldWidth: Int): Boolean {
    if (rangeWidth <= 0) return false
    if (x in rangeStart until rangeEnd) return true
    if (worldWidth <= 0) return false
    if (x + worldWidth in rangeStart until rangeEnd) return true
    if (x - worldWidth in rangeStart until rangeEnd) return true
    return false
}
