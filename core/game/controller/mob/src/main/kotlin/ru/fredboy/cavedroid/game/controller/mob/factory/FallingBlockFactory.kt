package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.abstraction.MobPhysicsFactory
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.behavior.FallingBlockMobBehavior
import javax.inject.Inject

@GameScope
class FallingBlockFactory @Inject constructor(
    private val mobController: MobController,
    private val mobPhysicsFactory: MobPhysicsFactory,
) {

    fun create(x: Float, y: Float, block: Block): FallingBlock {
        val fallingBlock = FallingBlock(
            block = block,
            behavior = FallingBlockMobBehavior(),
        ).apply { spawn(x, y, mobPhysicsFactory) }

        mobController.addMob(fallingBlock)

        return fallingBlock
    }
}
