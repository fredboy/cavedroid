package ru.fredboy.cavedroid.game.world.generator

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import kotlin.random.Random

/**
 * One independently seeded room per region at most. Callers supply complete regions of terrain
 * so floor/ceiling validation makes the same decision on either side of a streamed chunk seam.
 * Rooms may be sealed in stone; an existing cave entrance is deliberately not required.
 */
class MonsterRoomGenerator(
    private val config: WorldGeneratorConfig,
    private val items: ItemsRepository,
) {
    fun generate(startX: Int, fore: Array<Array<Block>>, back: Array<Array<Block>>, loopingWidth: Int? = null) {
        val minTop = maxOf(1, config.maxSurfaceHeight + 8)
        val maxTop = minOf(config.lavaLevel - ROOM_HEIGHT - 1, config.height - ROOM_HEIGHT - 1)
        if (fore.isEmpty() || minTop > maxTop) return

        val endX = startX + fore.size - 1
        for (region in Math.floorDiv(startX, REGION_WIDTH)..Math.floorDiv(endX, REGION_WIDTH)) {
            val random = Random(config.seed xor (region.toLong() * 341873128712L) xor 0x44554E47454F4EL)
            // Minecraft's 3D attempt density would overcrowd a 2D world without the entrance rule.
            if (random.nextBoolean()) continue
            val regionStart = region * REGION_WIDTH
            for (attempt in 0 until 4) {
                val width = if (random.nextBoolean()) 7 else 9
                val left = regionStart + random.nextInt(1, REGION_WIDTH - width)
                val top = random.nextInt(minTop, maxTop + 1)
                val right = left + width - 1
                if (loopingWidth != null && (left < 0 || right >= loopingWidth)) continue
                val localLeft = left - startX
                val localRight = right - startX
                if (localLeft !in fore.indices || localRight !in fore.indices) continue
                if (!canPlace(localLeft, localRight, top, fore, back)) continue
                buildRoom(localLeft, localRight, top, fore, back, random)
                break
            }
        }
    }

    private fun canPlace(left: Int, right: Int, top: Int, fore: Array<Array<Block>>, back: Array<Array<Block>>): Boolean {
        val floor = top + ROOM_HEIGHT - 1
        for (x in left..right) {
            for (y in listOf(top, floor)) {
                val block = fore[x][y]
                if (!block.params.hasCollision || !block.params.isFullBlock || block.isFluid()) return false
            }
            for (y in top..floor) {
                // Preserve existing mineshaft chests/spawners and keep water and lava out of rooms.
                if (fore[x][y] is Block.Container || back[x][y] is Block.Container || fore[x][y].isFluid()) return false
            }
        }
        return true
    }

    private fun buildRoom(
        left: Int,
        right: Int,
        top: Int,
        fore: Array<Array<Block>>,
        back: Array<Array<Block>>,
        random: Random,
    ) {
        val floor = top + ROOM_HEIGHT - 1
        val cobblestone = items.getBlockByKey("cobblestone")
        val mossyCobblestone = items.getBlockByKey("mossy_cobblestone")
        for (x in left..right) {
            for (y in top..floor) {
                val block = when {
                    y == floor -> if (random.nextInt(4) == 0) cobblestone else mossyCobblestone
                    y == top -> cobblestone
                    (x == left || x == right) && fore[x][y].params.hasCollision -> cobblestone
                    else -> items.fallbackBlock
                }
                fore[x][y] = block
                back[x][y] = if (y == floor) block else cobblestone
            }
        }

        // Two chests, each with three placement attempts; only a side wall counts as support.
        repeat(2) {
            for (attempt in 0 until 3) {
                val x = random.nextInt(left + 1, right)
                val y = floor - 1
                if (!fore[x][y].isNone() || back[x][y] is Block.Chest) continue
                val solidSides = listOf(x - 1, x + 1).count { fore[it][y].params.hasCollision }
                if (solidSides != 1) continue
                back[x][y] = items.getBlockByKey("chest_monster_room")
                break
            }
        }

        val spawner = when (random.nextInt(4)) {
            0, 1 -> "spawner_zombie"
            2 -> "spawner_skeleton"
            else -> "spawner"
        }
        fore[(left + right) / 2][floor - 1] = items.getBlockByKey(spawner)
    }

    companion object {
        const val REGION_WIDTH = 64
        private const val ROOM_HEIGHT = 6
    }
}
