package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseItemAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.model.Pig
import javax.inject.Inject

@GameScope
@BindUseItemAction(UsePigSpawnEggAction.ACTION_KEY)
class UsePigSpawnEggAction @Inject constructor(
    private val mobController: MobController,
    private val getPigSprites: GetPigSpritesUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        Pig(getPigSprites(), mobController.player.cursorX.px, mobController.player.cursorY.px)
            .apply { attachToController(mobController) }

        mobController.player.decreaseCurrentItemCount()
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg_pig"
    }
}
