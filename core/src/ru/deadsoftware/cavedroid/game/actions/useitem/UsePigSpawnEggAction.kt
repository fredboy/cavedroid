package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.Pig
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.misc.utils.px
import javax.inject.Inject

@GameScope
class UsePigSpawnEggAction @Inject constructor(
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        Pig(mobsController.player.cursorX.px, mobsController.player.cursorY.px)
            .apply {
                attachToController(mobsController)
            }

        mobsController.player.decreaseCurrentItemCount(gameItemsHolder)
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg_pig"
    }
}
