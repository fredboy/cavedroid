package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.fire.FireController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(Item.FlintAndSteel.USE_ACTION_KEY)
class UseFlintAndSteelAction @Inject constructor(
    private val fireController: FireController,
    private val mobController: MobController,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val ignited = fireController.ignite(x, y) != null
        if (ignited && !mobController.player.gameMode.isCreative()) {
            mobController.player.durateActiveDurable()
        }
        return ignited
    }
}
