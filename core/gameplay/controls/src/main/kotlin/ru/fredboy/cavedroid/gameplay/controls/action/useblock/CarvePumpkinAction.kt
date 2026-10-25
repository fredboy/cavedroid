package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.isShears
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.drop.DropController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(CarvePumpkinAction.ACTION_KEY)
class CarvePumpkinAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
    private val dropController: DropController,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean {
        val player = mobController.player
        if (block.params.key != "pumpkin" || player.activeItem.amount <= 0 || !player.activeItem.item.isShears()) return false
        val foreground = gameWorld.getForeMap(x, y)
        val carved = itemsRepository.getBlockByKey("carved_pumpkin")
        when {
            foreground.params.key == "pumpkin" -> gameWorld.setForeMap(x, y, carved)
            foreground.isNone() && gameWorld.getBackMap(x, y).params.key == "pumpkin" -> gameWorld.setBackMap(x, y, carved)
            else -> return false
        }
        dropController.addDrop(x + 0.5f, y + 0.5f, itemsRepository.getItemByKey("pumpkin_seeds").toInventoryItem(4))
        player.durateActiveDurable()
        return true
    }

    companion object {
        const val ACTION_KEY = "carve_pumpkin"
    }
}
