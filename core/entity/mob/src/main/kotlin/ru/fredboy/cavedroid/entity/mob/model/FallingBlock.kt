package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.utils.floor
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior
import ru.fredboy.cavedroid.entity.mob.abstraction.MobWorldAdapter

class FallingBlock(
    val block: Block,
    behavior: MobBehavior,
) : Mob(.9f, 1f, Direction.RIGHT, Int.MAX_VALUE, behavior) {

    override val speed get() = 0f

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

    override fun getControlVectorWithAppliedResistance(mobWorldAdapter: MobWorldAdapter): Vector2 {
        return controlVector
    }
}
