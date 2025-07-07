package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.behavior.FallingBlockMobBehavior
import javax.inject.Inject

@GameScope
class FallingBlockFactory @Inject constructor(
    private val mobController: MobController,
    private val mobWorldAdapter: MobWorldAdapter,
) {

    fun create(x: Float, y: Float, block: Block): FallingBlock {
        val fallingBlock = FallingBlock(
            block = block,
            behavior = FallingBlockMobBehavior(),
        ).apply { spawn(x, y, mobWorldAdapter.getBox2dWorld()) }

        mobController.addMob(fallingBlock)

        return fallingBlock
    }
}
