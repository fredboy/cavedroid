package ru.deadsoftware.cavedroid.game

import com.badlogic.gdx.utils.TimeUtils
import kotlin.math.abs
import kotlin.random.Random

private fun generateHeights(width: Int, min: Int, max: Int, random: Random) = IntArray(width).apply {
    set(0, (min + max) / 2)
    for (x in 1 until width) {
        val previous = get(x - 1)
        var d = random.nextInt(-5, 6).let { if (it !in -4..4) it / abs(it) else 0 }

        if (previous + d !in min..max) { d = -d }
        if (lastIndex - x < abs(get(0) - previous) * 3) {
            d = get(0).compareTo(previous).let { if (it != 0) it / abs(it) else 0 }
        }

        set(x, get(x - 1) + d)
    }
}

/**
 * Generates world of given width and height with given seed
 * @param width world width
 * @param height world height
 * @param seed seed for random number generator
 * @return pair of foreground and background layers
 */
fun generate(width: Int, height: Int, seed: Long = TimeUtils.millis()): Pair<Array<IntArray>, Array<IntArray>> {
    val random = Random(seed)
    val foreMap = Array(width) { IntArray(height) }
    val backMap = Array(width) { IntArray(width) }
    val heightsMap = generateHeights(width, height / 2, height * 3 / 4, random)

    for (x in 0 until width) {
        val xHeight = heightsMap[x]

        foreMap[x][xHeight] = GameItems.getBlockId("grass")
        foreMap[x][height - 1] = GameItems.getBlockId("bedrock")
        backMap[x][xHeight] = GameItems.getBlockId("grass")
        backMap[x][height - 1] = GameItems.getBlockId("bedrock")

        for (y in xHeight + 1 until height - 1) {
            foreMap[x][y] = when {
                y < xHeight + random.nextInt(5, 8) -> GameItems.getBlockId("dirt")
                else -> GameItems.getBlockId("stone")
            }
            backMap[x][y] = foreMap[x][y]
        }
    }
    return Pair(foreMap, backMap)
}
