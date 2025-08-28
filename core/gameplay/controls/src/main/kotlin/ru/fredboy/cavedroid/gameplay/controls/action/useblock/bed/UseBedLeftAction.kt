package ru.fredboy.cavedroid.gameplay.controls.action.useblock.bed

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.controls.action.annotation.BindUseBlockAction
import ru.fredboy.cavedroid.gameplay.controls.action.useblock.IUseBlockAction
import javax.inject.Inject

@GameScope
@BindUseBlockAction(stringKey = UseBedLeftAction.KEY)
class UseBedLeftAction @Inject constructor(
    private val gameWorld: GameWorld,
    private val mobController: MobController,
) : IUseBlockAction {

    override fun perform(block: Block, x: Int, y: Int) {
        mobController.player.spawnPoint = mobController.player.position

        if (gameWorld.isDayTime() ||
            !mobController.playerCanSleep() ||
            gameWorld.getForeMap(x + 1, y - 1).params.hasCollision
        ) {
            return
        }

        gameWorld.skipNight()
        mobController.player.isInBed = true
        mobController.player.applyPendingTransform(
            Vector2(
                (x.toFloat() + (2f - mobController.player.width / 2f)) - mobController.player.position.x,
                (y.toFloat() + 1 - mobController.player.height / 2f) - mobController.player.position.y,
            ),
        )
    }

    companion object {
        const val KEY = "bed_l"
    }
}
