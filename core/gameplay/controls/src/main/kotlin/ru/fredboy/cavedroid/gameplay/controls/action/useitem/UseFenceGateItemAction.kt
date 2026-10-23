package ru.fredboy.cavedroid.gameplay.controls.action.useitem

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.usecase.GetBlockByKeyUseCase
import ru.fredboy.cavedroid.domain.stats.repository.StatsRepository
import ru.fredboy.cavedroid.entity.mob.model.Direction
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseItemAction
import javax.inject.Inject

@GameScope
@BindUseItemAction(UseFenceGateItemAction.ACTION_KEY)
class UseFenceGateItemAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
    private val getBlockByKey: GetBlockByKeyUseCase,
    private val statsRepository: StatsRepository,
) : IUseItemAction {

    override fun perform(item: Item.Usable, x: Int, y: Int): Boolean {
        val side = if (mobController.player.direction == Direction.RIGHT) "left" else "right"
        val block = getBlockByKey["${item.params.key}_${side}_closed"]
        val rectangle = block.getRectangle(x, y)
        if (mobController.player.hitbox.overlaps(rectangle) || mobController.mobs.any { it.hitbox.overlaps(rectangle) }) {
            return false
        }

        if (!gameWorld.placeToForeground(x, y, block)) return false
        mobController.player.decreaseCurrentItemCount()
        statsRepository.recordBlockPlaced()
        return true
    }

    companion object {
        const val ACTION_KEY = "use_fence_gate_item"
    }
}
