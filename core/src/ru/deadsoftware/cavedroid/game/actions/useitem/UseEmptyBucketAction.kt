package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class UseEmptyBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        if (!foregroundBlock.isFluid()) {
            return
        }
        gameWorld.resetForeMap(x, y)

        val filled = when (foregroundBlock) {
            is Block.Lava -> gameItemsHolder.getItem("bucket_lava")
            is Block.Water -> gameItemsHolder.getItem("bucket_water")
            else -> throw IllegalStateException("unknown fluid")
        }

        mobsController.player.setCurrentInventorySlotItem(filled)
    }

    companion object {
        const val ACTION_KEY = "use_empty_bucket"
    }
}
