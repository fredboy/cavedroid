package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@UseItemAction(UseLavaBucketAction.ACTION_KEY)
class UseLavaBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        gameWorld.placeToForeground(x, y, gameItemsHolder.getBlock("lava"))

        if (mobsController.player.gameMode != 1) {
            mobsController.player.setCurrentInventorySlotItem(gameItemsHolder.getItem("bucket_empty"))
        }
    }

    companion object {
        const val ACTION_KEY = "use_lava_bucket"
    }
}
