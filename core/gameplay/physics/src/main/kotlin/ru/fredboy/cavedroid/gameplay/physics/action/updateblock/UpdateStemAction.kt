package ru.fredboy.cavedroid.gameplay.physics.action.updateblock

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.StemBlocks
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.physics.action.annotation.BindUpdateBlockAction
import javax.inject.Inject

@GameScope
@BindUpdateBlockAction(StemBlocks.UPDATE_ACTION_KEY)
class UpdateStemAction @Inject constructor(
    private val gameWorld: GameWorld,
) : IUpdateBlockAction {

    override fun update(x: Int, y: Int) {
        if (StemBlocks.fromKey(gameWorld.getForeMap(x, y).params.key) == null) return
        if (gameWorld.getForeMap(x, y + 1).params.key !in StemBlocks.FARMLAND_KEYS) {
            gameWorld.destroyForeMap(x, y, true)
        }
    }
}
