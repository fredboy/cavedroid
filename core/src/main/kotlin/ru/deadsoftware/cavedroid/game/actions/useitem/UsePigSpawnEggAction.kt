package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.misc.annotations.multibind.BindUseItemAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.factory.PigFactory
import javax.inject.Inject

@GameScope
@BindUseItemAction(UsePigSpawnEggAction.ACTION_KEY)
class UsePigSpawnEggAction @Inject constructor(
    private val mobController: MobController,
    private val pigFactory: PigFactory,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        pigFactory.create(mobController.player.cursorX.px, mobController.player.cursorY.px)
        mobController.player.decreaseCurrentItemCount()
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg_pig"
    }
}
