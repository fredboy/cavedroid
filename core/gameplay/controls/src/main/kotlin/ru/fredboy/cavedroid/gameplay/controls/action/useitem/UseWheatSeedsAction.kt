package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseWheatSeedsAction.ACTION_KEY)
class UseWheatSeedsAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val plantY = when {
            gameWorld.getForeMap(x, y).isNone() &&
                gameWorld.getForeMap(x, y + 1).params.key in FARMLAND_KEYS -> y
            gameWorld.getForeMap(x, y).params.key in FARMLAND_KEYS &&
                gameWorld.getForeMap(x, y - 1).isNone() -> y - 1
            else -> return false
        }

        gameWorld.setForeMap(x, plantY, itemsRepository.getBlockByKey("wheat_stage0"))
        mobController.player.decreaseCurrentItemCount()
        return true
    }

    companion object {
        const val ACTION_KEY = "use_wheat_seeds_action"
        private val FARMLAND_KEYS = setOf("farmland", "farmland_moist")
    }
}
