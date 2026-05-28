package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.item.isNone
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

private fun tillSoil(
    gameWorld: GameWorld,
    mobController: MobController,
    itemsRepository: ItemsRepository,
    x: Int,
    y: Int,
): Boolean {
    if (mobController.player.activeItem.item !is Item.Hoe) {
        return false
    }
    if (!gameWorld.getForeMap(x, y - 1).isNone()) {
        return false
    }

    gameWorld.setForeMap(x, y, itemsRepository.getBlockByKey("farmland"))
    mobController.player.durateActiveDurable()
    return true
}

@GameScope
@BindUseBlockAction(stringKey = UseHoeOnDirtAction.KEY)
class UseHoeOnDirtAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean = tillSoil(gameWorld, mobController, itemsRepository, x, y)

    companion object {
        const val KEY = "dirt"
    }
}

@GameScope
@BindUseBlockAction(stringKey = UseHoeOnGrassAction.KEY)
class UseHoeOnGrassAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean = tillSoil(gameWorld, mobController, itemsRepository, x, y)

    companion object {
        const val KEY = "grass"
    }
}
