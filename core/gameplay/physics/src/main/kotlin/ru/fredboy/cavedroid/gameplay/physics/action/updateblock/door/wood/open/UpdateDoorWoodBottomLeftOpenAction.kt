package ru.fredboy.cavedroid.gameplay.physics.action.updateblock.door.wood.open

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import ru.fredboy.cavedroid.gameplay.physics.action.updateblock.IUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(stringKey = UpdateDoorWoodBottomLeftOpenAction.BLOCK_KEY)
class UpdateDoorWoodBottomLeftOpenAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val getBlockByKeyUseCase: GetBlockByKeyUseCase,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        val top = getBlockByKeyUseCase["wood_door_top_left_open"]
        if (gameWorld.getForeMap(x, y - 1) != top) {
            gameWorld.resetForeMap(x, y)
        }
    }

    companion object {
        const val BLOCK_KEY = "wood_door_bottom_left_open"
    }
}
