package ru.fredboy.cavedroid.gameplay.physics.action.updateblock.sapling

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateSaplingBirchAction.BLOCK_KEY)
class UpdateSaplingBirchAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y + 1).params.key.let { it != "dirt" && it != "grass" && it != "grass_snowed" } ||
            MathUtils.randomBoolean(0.99f)
        ) {
            return
        }

        for (ix in x - 1..x + 1) {
            for (iy in y - 5..y) {
                if (!gameWorld.getForeMap(ix, iy).params.replaceable) {
                    return
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
    }

    companion object {
        const val BLOCK_KEY = "sapling_birch"
    }
}
