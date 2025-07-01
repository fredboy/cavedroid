package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.entity.mob.abstraction.MobBehavior

class FallingBlock(
    val block: Block,
    x: Float,
    y: Float,
    behavior: MobBehavior,
) : Mob(x, y, 1.px, 1.px, Direction.RIGHT, Int.MAX_VALUE, behavior) {

    init {
        velocity.y = 1f
    }

    override val speed get() = 0f

    override fun changeDir() = Unit

    override fun jump() = Unit

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
    ) {
        block.draw(spriteBatch, x, y)
    }
}
