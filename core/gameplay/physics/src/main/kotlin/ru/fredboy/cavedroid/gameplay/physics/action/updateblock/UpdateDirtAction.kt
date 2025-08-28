package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateDirtAction.BLOCK_KEY)
class UpdateDirtAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (!gameWorld.getForeMap(x, y - 1).params.replaceable || MathUtils.randomBoolean(0.95f)) {
            return
        }

        for (ix in x - 1..x + 1) {
            for (iy in y - 1..y + 1) {
                if (gameWorld.getForeMap(ix, iy).params.key == "grass") {
                    gameWorld.setForeMap(x, y, getBlockByKeyUseCase["grass"])
                }
            }
        }
    }

    companion object {
        const val BLOCK_KEY = "dirt"
    }
}
