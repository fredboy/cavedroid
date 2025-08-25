package ru.fredboy.cavedroid.entity.mob.impl

import ru.fredboy.cavedroid.entity.mob.abstraction.BaseMobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock

class FallingBlockMobBehavior :
    BaseMobBehavior<FallingBlock>(
        mobType = FallingBlock::class,
    ) {

    override fun FallingBlock.updateMob(worldAdapter: MobWorldAdapter, playerAdapter: PlayerAdapter, delta: Float) {
        if (velocity.isZero) {
            worldAdapter.setForegroundBlock(mapX, middleMapY, block)
            kill()
        }
    }
}
