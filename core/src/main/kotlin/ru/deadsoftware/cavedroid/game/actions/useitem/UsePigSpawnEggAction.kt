package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.Pig
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseItemAction
import ru.fredboy.cavedroid.domain.assets.usecase.GetPigSpritesUseCase
import ru.fredboy.cavedroid.utils.px
import javax.inject.Inject

@GameScope
@BindUseItemAction(UsePigSpawnEggAction.ACTION_KEY)
class UsePigSpawnEggAction @Inject constructor(
    private val mobsController: MobsController,
    private val gameItemsHolder: GameItemsHolder,
    private val getPigSprites: GetPigSpritesUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        Pig(getPigSprites(), mobsController.player.cursorX.px, mobsController.player.cursorY.px)
            .apply {
                attachToController(mobsController)
            }

        mobsController.player.decreaseCurrentItemCount(gameItemsHolder)
    }

    companion object {
        const val ACTION_KEY = "use_spawn_egg_pig"
    }
}
