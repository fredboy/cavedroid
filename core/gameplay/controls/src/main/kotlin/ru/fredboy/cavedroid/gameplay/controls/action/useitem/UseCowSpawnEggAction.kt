package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.factory.CowFactory
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseCowSpawnEggAction.ACTION_KEY)
class UseCowSpawnEggAction @Inject constructor(
    private val mobController: MobController,
    private val cowFactory: CowFactory,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        cowFactory.create(mobController.player.cursorX.toFloat(), mobController.player.cursorY.toFloat())
        mobController.player.decreaseCurrentItemCount()
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg_cow"
    }
}
