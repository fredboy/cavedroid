package ru.fredboy.cavedroid.gameplay.physics.action.growblock.sapling

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = GrowSaplingSpruceAction.BLOCK_KEY)
class GrowSaplingSpruceAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IGrowBlockAction {

    override fun grow(x: Int, y: Int): Boolean {
        if (gameWorld.getForeMap(x, y).params.key != BLOCK_KEY) {
            return false
        }
        if (gameWorld.getForeMap(x, y + 1).params.key !in SOIL_KEYS) {
            return false
        }

        for (ix in x - 2..x + 2) {
            for (iy in y - 7..y) {
                if (ix == x && iy == y) continue
                if (!gameWorld.getForeMap(ix, iy).params.replaceable) {
                    return false
                }
            }
        }

        gameWorld.resetForeMap(x, y)

        val leaves = getBlockByKeyUseCase["leaves_spruce"]
        val log = getBlockByKeyUseCase["log_spruce"]

        for (iy in y downTo y - 6) {
            gameWorld.setBackMap(x, iy, log)
        }

        gameWorld.setBackMap(x, y - 7, leaves)
        gameWorld.setForeMap(x, y - 7, leaves)

        for (ix in x - 1..x + 1) {
            val iy = y - 6
            if (ix != x) {
                gameWorld.setBackMap(ix, iy, leaves)
            }
            gameWorld.setForeMap(ix, iy, leaves)
        }

        for (iy in 1..2) {
            for (ix in x - iy..x + iy) {
                if (ix != x) {
                    gameWorld.setBackMap(ix, y - 5 + iy, leaves)
                }
                gameWorld.setForeMap(ix, y - 5 + iy, leaves)
            }
        }

        return true
    }

    companion object {
        const val BLOCK_KEY = "sapling_spruce"
        private val SOIL_KEYS = setOf("dirt", "grass", "grass_snowed")
    }
}
