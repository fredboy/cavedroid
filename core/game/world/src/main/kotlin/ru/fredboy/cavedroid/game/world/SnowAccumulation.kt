package ru.fredboy.cavedroid.game.world

import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import kotlin.random.Random

internal class SnowAccumulation(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
    private val random: Random = Random.Default,
) {
    private var elapsed = 0f

    fun update(delta: Float) {
        if (gameWorld.weather != Weather.RAIN || gameWorld.weatherIntensity <= 0f) {
            elapsed = 0f
            return
        }

        elapsed += delta * gameWorld.weatherIntensity
        if (elapsed < UPDATE_INTERVAL_SEC) return
        elapsed %= UPDATE_INTERVAL_SEC

        // Sample one column per chunk so surfaces acquire snow gradually.
        if (gameWorld.isInfinite) {
            gameWorld.forEachLoadedChunk { chunkX ->
                coverColumn(chunkX * ChunkGenerator.CHUNK_W + random.nextInt(ChunkGenerator.CHUNK_W))
            }
        } else {
            for (startX in gameWorld.start..gameWorld.end step ChunkGenerator.CHUNK_W) {
                val columns = minOf(ChunkGenerator.CHUNK_W, gameWorld.end - startX + 1)
                coverColumn(startX + random.nextInt(columns))
            }
        }
    }

    private fun coverColumn(x: Int) {
        if (gameWorld.getBiomeAt(x) != Biome.WINTER) return

        val lowestSurface = minOf(gameWorld.generatorConfig.maxSurfaceHeight, gameWorld.height - 1)
        for (y in 0..lowestSurface) {
            val block = gameWorld.getForeMap(x, y)
            if (block.isNone()) continue

            val margins = block.params.collisionMargins
            if (y > 0 && block.params.hasCollision && margins.top == 0f && margins.left == 0f && margins.right == 0f) {
                gameWorld.setForeMap(x, y - 1, itemsRepository.getBlockByKey("snow"))
            }
            // Only the first occupied cell sees snowfall; never bury plants or stack thin snow.
            return
        }
    }

    companion object {
        private const val UPDATE_INTERVAL_SEC = 1f
    }
}
