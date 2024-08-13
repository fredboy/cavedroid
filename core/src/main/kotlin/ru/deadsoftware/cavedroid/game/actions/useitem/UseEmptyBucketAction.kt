package ru.deadsoftware.cavedroid.game.actions.useitem

import ru.deadsoftware.cavedroid.misc.annotations.multibinding.BindUseItemAction
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseEmptyBucketAction.ACTION_KEY)
class UseEmptyBucketAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getItemByKeyUseCase: GetItemByKeyUseCase,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        if (!foregroundBlock.isFluid()) {
            return
        }
        gameWorld.resetForeMap(x, y)

        val filled = when (foregroundBlock) {
            is Block.Lava -> getItemByKeyUseCase["bucket_lava"]
            is Block.Water -> getItemByKeyUseCase["bucket_water"]
            else -> throw IllegalStateException("unknown fluid")
        }

        mobController.player.setCurrentInventorySlotItem(filled)
    }

    companion object {
        const val ACTION_KEY = "use_empty_bucket"
    }
}
