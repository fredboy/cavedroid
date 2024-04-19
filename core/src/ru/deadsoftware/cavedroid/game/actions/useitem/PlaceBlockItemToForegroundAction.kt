package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.objects.Item
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class PlaceBlockItemToForegroundAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUseItemAction {

    override fun perform(item: Item, x: Int, y: Int) {
        val block = item.toBlock()
        requireNotNull(block) { "error: trying to place non block item" }
        gameWorld.placeToForeground(x, y, block.id)
    }

    companion object {
        const val ACTION_KEY = "place_foreground_block"
    }

}
