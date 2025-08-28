package ru.fredboy.cavedroid.gameplay.controls.action.useblock.door.wood.open

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useblock.IUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseDoorWoodBottomRightOpenAction.KEY)
class UseDoorWoodBottomRightOpenAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        if (gameWorld.getForeMap(x, y - 1).params.key != "door_wood_top_right_open") {
            return
        }

        gameWorld.setForeMap(x, y - 1, itemsRepository.getBlockByKey("door_wood_top_right_closed"))
        gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("door_wood_bottom_right_closed"))
    }

    companion object {
        const val KEY = "door_wood_bottom_right_open"
    }
}
