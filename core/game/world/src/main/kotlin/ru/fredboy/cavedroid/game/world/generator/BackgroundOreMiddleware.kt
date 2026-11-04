package ru.fredboy.cavedroid.game.world.generator

import kotlin.math.abs

/** Adds mineable background veins without consuming terrain RNG or changing generation bands. */
object BackgroundOreMiddleware : WorldGenerationMiddleware {
    override val id = "background_ores"
    override val salt = 0x4241434B4F5245L
    override val horizontalReach = 4

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        val firstY = maxOf(0, config.seaLevel + 1)
        val bottom = config.height - 1
        if (backMap.isEmpty() || firstY >= bottom) return@with

        val stone = items.getBlockByKey("stone")
        val coal = items.getBlockByKey("ore_coal")
        val iron = items.getBlockByKey("ore_iron")
        val gold = items.getBlockByKey("ore_gold")
        val diamond = items.getBlockByKey("ore_diamond")
        val lapis = items.getBlockByKey("ore_lapis")

        // Include origins outside the band that can reach its left edge. Looping worlds clip
        // veins at their world bounds, like the existing foreground ore generators.
        for (x in startX - horizontalReach until startX + backMap.size) {
            if (loopingWidth != null && x !in 0 until loopingWidth) continue
            val rng = random.column(x, 0)
            for (y in firstY until bottom) {
                val roll = rng.nextInt(10000)
                val depth = config.height - y
                val (ore, size) = when {
                    roll in 0 until 50 && depth < 32 -> diamond to 2
                    roll in 50 until 100 && depth < 64 -> gold to 3
                    roll in 100 until 250 && depth < 128 -> iron to 3
                    roll in 250 until 400 && depth in 33 until 192 -> coal to 3
                    roll in 600 until 600 + (32 - abs(depth - 16) * 2) -> lapis to 4
                    else -> continue
                }
                val width = rng.nextInt(1, size + 1)
                val height = rng.nextInt(1, size + 1)
                for (dx in 0..width) {
                    for (dy in 0..height) {
                        // Draw even for clipped or occupied cells: plans must depend only on
                        // world coordinates, never on band size or earlier replacements.
                        val place = rng.nextDouble() < 0.5
                        val localX = x + dx - startX
                        val targetY = y + dy
                        if (place && localX in backMap.indices && targetY < bottom && backMap[localX][targetY] == stone) {
                            backMap[localX][targetY] = ore
                        }
                    }
                }
            }
        }
    }
}
