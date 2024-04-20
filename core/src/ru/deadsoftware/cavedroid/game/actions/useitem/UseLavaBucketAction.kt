package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItems
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UseLavaBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        gameWorld.placeToForeground(x, y, GameItems.getBlockId("lava"))
        mobsController.player.setCurrentInventorySlotItem(GameItems.getItemId("bucket_empty"))
    }

    companion object {
        const val ACTION_KEY = "use_lava_bucket"
    }
}
