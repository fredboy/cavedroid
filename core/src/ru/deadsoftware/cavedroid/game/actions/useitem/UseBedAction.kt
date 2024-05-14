package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@UseItemAction(UseBedAction.ACTION_KEY)
class UseBedAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        val bedLeft = gameItemsHolder.getBlock("bed_l")
        val bedRight = gameItemsHolder.getBlock("bed_r")

        if (gameWorld.canPlaceToForeground(x, y, bedLeft) && gameWorld.canPlaceToForeground(x + 1, y, bedRight)) {
            gameWorld.placeToForeground(x, y, bedLeft)
            gameWorld.placeToForeground(x + 1, y, bedRight)
            mobsController.player.inventory.decreaseCurrentItemAmount()
        }
    }

    companion object {
        const val ACTION_KEY = "use_bed_action"
    }
}
