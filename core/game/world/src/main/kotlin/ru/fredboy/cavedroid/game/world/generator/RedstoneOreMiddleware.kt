package ru.fredboy.cavedroid.game.world.generator

/** Independent deposits preserve the existing terrain and structure random streams. */
object RedstoneOreMiddleware : WorldGenerationMiddleware {
    override val id = "redstone_ore"
    override val salt = 0x52454453544F4EL
    override val horizontalReach = 3

    override fun generate(context: WorldGenerationContext, random: GenerationRandom) = with(context) {
        val firstY = maxOf(config.seaLevel + 1, config.height - 32, 0)
        val bottom = config.height - 1
        if (foreMap.isEmpty() || firstY >= bottom) return@with

        val stone = items.getBlockByKey("stone")
        val redstone = items.getBlockByKey("ore_redstone")
        for (x in startX - horizontalReach until startX + foreMap.size) {
            if (loopingWidth != null && x !in 0 until loopingWidth) continue
            val rng = random.column(x, 0)
            for (y in firstY until bottom) {
                if (rng.nextInt(100) != 0) continue
                val width = rng.nextInt(1, horizontalReach + 1)
                val height = rng.nextInt(1, 4)
                for (dx in 0..width) {
                    for (dy in 0..height) {
                        // Consume every draw even when this band clips a deposit.
                        val place = rng.nextDouble() < 0.5
                        val localX = x + dx - startX
                        val targetY = y + dy
                        if (!place || localX !in foreMap.indices || targetY >= bottom) continue
                        if (foreMap[localX][targetY] == stone) foreMap[localX][targetY] = redstone
                        if (backMap[localX][targetY] == stone) backMap[localX][targetY] = redstone
                    }
                }
            }
        }
    }
}
