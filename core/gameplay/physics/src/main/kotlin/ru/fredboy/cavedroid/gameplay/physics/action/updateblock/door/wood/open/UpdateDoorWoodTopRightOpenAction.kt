package ru.fredboy.cavedroid.gameplay.physics.action.updateblock.door.wood.open

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateDoorWoodTopRightOpenAction.BLOCK_KEY)
class UpdateDoorWoodTopRightOpenAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val bottom = getBlockByKeyUseCase["wood_door_bottom_right_open"]
        if (gameWorld.getForeMap(x, y + 1) != bottom) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "wood_door_top_right_open"
    }
}
