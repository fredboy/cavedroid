package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseWaterBucketAction.ACTION_KEY)
class UseWaterBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        gameWorld.placeToForeground(x, y, gameItemsHolder.getBlock("water"))
        if (mobsController.player.gameMode != 1) {
            mobsController.player.setCurrentInventorySlotItem(gameItemsHolder.getItem("bucket_empty"))
        }
    }

    companion object {
        const val ACTION_KEY = "use_water_bucket"
    }

}
