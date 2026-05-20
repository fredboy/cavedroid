package ru.fredboy.cavedroid.gameplay.physics.action.growblock.sapling

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindGrowBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.growblock.IGrowBlockAction
import javax.inject.Inject

@GameScope
@BindGrowBlockAction(stringKey = GrowSaplingBirchAction.BLOCK_KEY)
class GrowSaplingBirchAction @Inject constructor(
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

        for (ix in x - 1..x + 1) {
            for (iy in y - 5..y) {
                if (ix == x && iy == y) continue
                if (!gameWorld.getForeMap(ix, iy).params.replaceable) {
                    return false
                }
            }
        }

        gameWorld.resetForeMap(x, y)

        val leaves = getBlockByKeyUseCase["leaves_birch"]
        val log = getBlockByKeyUseCase["log_birch"]

        for (iy in y downTo y - 4) {
            gameWorld.setBackMap(x, iy, log)
        }

        gameWorld.setBackMap(x, y - 5, leaves)
        gameWorld.setForeMap(x, y - 5, leaves)

        for (ix in x - 1..x + 1) {
            for (iy in y - 4..y - 3) {
                if (ix != x) {
                    gameWorld.setBackMap(ix, iy, leaves)
                }
                gameWorld.setForeMap(ix, iy, leaves)
            }
        }

        return true
    }

    companion object {
        const val BLOCK_KEY = "sapling_birch"
        private val SOIL_KEYS = setOf("dirt", "grass", "grass_snowed")
    }
}
