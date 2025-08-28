package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseTrapdoorOpenAction.KEY)
class UseTrapdoorOpenAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val itemsRepository: ItemsRepository,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("trapdoor_wood_closed"))
    }

    companion object {
        const val KEY = "trapdoor_wood_open"
    }
}
