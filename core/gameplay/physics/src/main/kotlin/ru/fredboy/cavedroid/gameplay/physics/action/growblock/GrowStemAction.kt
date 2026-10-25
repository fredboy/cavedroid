package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.StemBlocks
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.random.Random

@GameScope
@BindGrowBlockAction(StemBlocks.GROW_ACTION_KEY)
class GrowStemAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlock: GetBlockByKeyUseCase,
) : IGrowBlockAction {

    // Java random ticks: three selections from 4096 blocks per 1/20-second tick.
    // Round up to the existing growth queue's one-second resolution.
    override fun nextDelayTicks(): Long = ceil(ln(1.0 - Random.nextDouble()) / (3.0 * ln(4095.0 / 4096.0)) / 20.0)
        .toLong().coerceAtLeast(1L)

    override fun grow(x: Int, y: Int): Boolean = grow(x, y, Random.Default)

    internal fun grow(x: Int, y: Int, random: Random): Boolean {
        val stem = StemBlocks.fromKey(gameWorld.getForeMap(x, y).params.key) ?: return false
        val soil = gameWorld.getForeMap(x, y + 1).params.key
        if (soil !in StemBlocks.FARMLAND_KEYS) return false
        if (gameWorld.getLightAt(x, y) < StemBlocks.MIN_GROWTH_LIGHT) return false
        if (!gameWorld.isChunkLoadedAt(x - 1) || !gameWorld.isChunkLoadedAt(x + 1)) return false

        val bound = StemBlocks.growthBound(
            gameWorld.getForeMap(x - 1, y + 1).params.key,
            soil,
            gameWorld.getForeMap(x + 1, y + 1).params.key,
        )
        if (random.nextInt(bound) != 0) return false

        if (stem.age < StemBlocks.MAX_AGE) {
            gameWorld.setForeMap(x, y, getBlock[stem.advance(1).key])
            return true
        }

        if (gameWorld.getForeMap(x - 1, y).params.key == stem.fruit ||
            gameWorld.getForeMap(x + 1, y).params.key == stem.fruit
        ) {
            return false
        }

        val fruitX = x + if (random.nextBoolean()) 1 else -1
        if (!gameWorld.getForeMap(fruitX, y).isNone()) return false
        val fruitSoil = gameWorld.getForeMap(fruitX, y + 1).params.key
        if (fruitSoil !in StemBlocks.FRUIT_SOIL_KEYS) return false

        gameWorld.setForeMap(fruitX, y, getBlock[stem.fruit])
        if (fruitSoil in StemBlocks.FARMLAND_KEYS) {
            gameWorld.setForeMap(fruitX, y + 1, getBlock["dirt"])
        }
        return false // The mature stem must remain scheduled for its next harvest.
    }
}
