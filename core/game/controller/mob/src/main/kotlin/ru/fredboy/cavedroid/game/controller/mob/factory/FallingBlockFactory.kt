package ru.fredboy.cavedroid.game.controller.mob.factory

import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.model.FallingBlock
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.controller.mob.behavior.FallingBlockMobBehavior
import javax.inject.Inject

@GameScope
class FallingBlockFactory @Inject constructor(
    private val mobController: MobController,
) {

    fun create(x: Int, y: Int, block: Block): FallingBlock {
        val fallingBlock = FallingBlock(
            block = block,
            x = x.px,
            y = y.px,
            behavior = FallingBlockMobBehavior(),
        )

        mobController.addMob(fallingBlock)

        return fallingBlock
    }
}
