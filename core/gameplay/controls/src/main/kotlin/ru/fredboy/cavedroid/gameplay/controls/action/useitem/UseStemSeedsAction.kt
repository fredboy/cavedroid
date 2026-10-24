package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.StemBlocks
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import ru.fredboy.cavedroid.gameplay.controls.usecase.PlantCropInteractor
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseStemSeedsAction.ACTION_KEY)
class UseStemSeedsAction @Inject constructor(
    private val mobController: MobController,
    private val plantCropInteractor: PlantCropInteractor,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val stem = StemBlocks.fromSeeds(item.params.key) ?: return false
        return plantCropInteractor.execute(mobController.player, stem.key, x, y)
    }

    companion object {
        const val ACTION_KEY = "use_stem_seeds"
    }
}
