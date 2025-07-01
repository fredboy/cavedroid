package ru.fredboy.cavedroid.game.controller.mob.behavior

import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock

class FallingBlockMobBehavior : BaseMobBehavior<FallingBlock>(
    mobType = FallingBlock::class,
) {

    override fun FallingBlock.updateMob(worldAdapter: MobWorldAdapter, delta: Float) {
        if (velocity.isZero) {
            worldAdapter.setForegroundBlock(mapX, middleMapY, block)
            kill()
        }
    }
}
