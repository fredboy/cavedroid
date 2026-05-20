package ru.fredboy.cavedroid.gameplay.controls.action.useblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.model.Player
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseCakeAction.KEY)
class UseCakeAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int): Boolean {
        if (mobController.player.foodLevel >= Player.MAX_FOOD_LEVEL) {
            return false
        }
        mobController.player.eat(HEAL, SATURATION)
        gameWorld.resetForeMap(x, y)
        return true
    }

    companion object {
        const val KEY = "cake"
        private const val HEAL = 14
        private const val SATURATION = 2.8f
    }
}
