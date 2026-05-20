package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseSugarCaneAction.ACTION_KEY)
class UseSugarCaneAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val itemsRepository: ItemsRepository,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val targetY = when {
            gameWorld.getForeMap(x, y).isNone() -> y
            gameWorld.getForeMap(x, y - 1).isNone() -> y - 1
            else -> return false
        }

        val belowKey = gameWorld.getForeMap(x, targetY + 1).params.key

        val placementValid = when {
            belowKey == "sugar_cane" -> true
            belowKey in SUPPORT_KEYS -> isWaterAdjacent(x, targetY + 1)
            else -> false
        }

        if (!placementValid) return false

        gameWorld.setForeMap(x, targetY, itemsRepository.getBlockByKey("sugar_cane"))
        mobController.player.decreaseCurrentItemCount()
        return true
    }

    private fun isWaterAdjacent(x: Int, y: Int): Boolean = gameWorld.getForeMap(x - 1, y).isWater() ||
        gameWorld.getForeMap(x + 1, y).isWater()

    companion object {
        const val ACTION_KEY = "use_sugar_cane_action"
        private val SUPPORT_KEYS = setOf("dirt", "grass", "grass_snowed", "sand")
    }
}
