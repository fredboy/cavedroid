package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import kotlin.random.Random

/** Each region's complete plan is seeded independently, then clipped to the requested columns. */
class MineshaftGenerator(
    private val config: WorldGeneratorConfig,
    private val items: ItemsRepository,
) {
    fun generate(startX: Int, fore: Array<Array<Block>>, back: Array<Array<Block>>, loopingWidth: Int? = null) {
        val minY = maxOf(config.seaLevel + 12, config.maxSurfaceHeight + 8)
        val maxY = minOf(config.lavaLevel - 20, config.height - 24)
        if (minY > maxY || fore.isEmpty()) return
        val endX = startX + fore.size - 1
        for (region in Math.floorDiv(startX, REGION_WIDTH)..Math.floorDiv(endX, REGION_WIDTH)) {
            val random = Random(config.seed xor (region.toLong() * 341873128712L) xor 0x4D494E45L)
            if (random.nextInt(4) == 0) continue
            val origin = region * REGION_WIDTH + random.nextInt(12, 33)
            val length = random.nextInt(64, 113)
            // Small looping worlds must not be left with half a structure at their edge.
            if (loopingWidth != null && (origin < 0 || origin + length >= loopingWidth)) continue
            val top = random.nextInt(minY, maxY + 1)
            val connector = origin + length / 2

            fun put(x: Int, y: Int, key: String, background: Boolean = false) {
                val localX = x - startX
                if (localX !in fore.indices || y !in 1 until config.height - 1) return
                val map = if (background) back else fore
                map[localX][y] = if (key == "none") items.fallbackBlock else items.getBlockByKey(key)
            }

            fun corridor(left: Int, right: Int, ceiling: Int) {
                for (x in left..right) {
                    put(x, ceiling, "planks_oak")
                    put(x, ceiling + 4, "planks_oak")
                    for (y in ceiling + 1..ceiling + 3) {
                        put(x, y, "none")
                        put(x, y, "none", background = true)
                    }
                    if ((x - left) % 6 == 0) {
                        for (y in ceiling + 1..ceiling + 3) put(x, y, "fence_oak", background = true)
                        // Keep the spider nest beyond the torch's 13-block light radius.
                        if ((x - left) % 24 == 6 && x <= right - 24) {
                            put(x, ceiling + 2, "torch")
                        }
                    } else if (random.nextInt(7) == 0) {
                        put(x, ceiling + 1, "web")
                    }
                }
            }

            corridor(origin, origin + length, top)
            corridor(origin + 12, origin + length - 8, top + 10)
            // Two-block shaft with a ladder joins the floors; stone provides attachment backing.
            for (y in top + 1..top + 13) {
                for (x in connector..connector + 1) {
                    put(x, y, "stone", background = true)
                    put(x, y, if (x == connector) "ladder" else "none")
                }
            }
            put(origin + 8, top + 3, "chest_mineshaft", background = true)
            put(origin + length - 13, top + 13, "chest_mineshaft", background = true)
            val spawnerX = origin + length - 6
            put(spawnerX, top + 3, "spawner")
            for (x in spawnerX - 3..spawnerX + 3) {
                put(x, top + 1, "web")
            }
        }
    }

    companion object {
        const val REGION_WIDTH = 192
    }
}
