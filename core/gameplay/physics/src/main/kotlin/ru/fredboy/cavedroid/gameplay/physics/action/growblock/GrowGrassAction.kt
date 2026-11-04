package ru.fredboy.cavedroid.gameplay.physics.action.growblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = GrowGrassAction.BLOCK_KEY)
class GrowGrassAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {

    override fun grow(x: Int, y: Int): Boolean {
        if (gameWorld.getForeMap(x, y).params.key != BLOCK_KEY) return false
        val above = gameWorld.getForeMap(x, y - 1)
        if (!above.params.replaceable || above.isFluid()) return false

        for (ix in x - 1..x + 1) {
            if (!gameWorld.isChunkLoadedAt(ix)) continue
            for (iy in y - 1..y + 1) {
                if (gameWorld.getForeMap(ix, iy).params.key in GRASS_KEYS) {
                    val key = if (above.params.key == "snow") "grass_snowed" else "grass"
                    gameWorld.setForeMap(x, y, getBlockByKeyUseCase[key])
                    return true
                }
            }
        }
        return false
    }

    companion object {
        const val BLOCK_KEY = "dirt"
        private val GRASS_KEYS = setOf("grass", "grass_snowed")
    }
}
