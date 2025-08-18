package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.floor
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobDropInfo
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter
import ru.fredboy.cavedroid.entity.mob.impl.FallingBlockMobBehavior

class FallingBlock(
    val block: Block,
) : Mob(Direction.RIGHT, getParams(block), FallingBlockMobBehavior()) {

    override fun changeDir() = Unit

    override fun jump() = Unit

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
    ) {
        block.draw(spriteBatch, x.floor, y)
    }

    override fun applyMediumResistanceToBody(mobWorldAdapter: MobWorldAdapter) = Unit

    companion object {
        private fun getParams(block: Block): MobParams {
            return MobParams(
                name = block.params.key,
                key = block.params.key,
                width = 0.9f,
                height = 0.9f,
                speed = 0f,
                behaviorType = MobBehaviorType.FALLING_BLOCK,
                dropInfo = MobDropInfo(
                    itemKey = "none",
                    count = 0,
                ),
                hp = Int.MAX_VALUE,
                sprites = emptyList(),
            )
        }
    }
}
