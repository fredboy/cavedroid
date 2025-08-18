package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.entity.mob.impl.PassiveMobBehavior

abstract class AbstractPassiveMob(
    width: Float,
    height: Float,
    maxHealth: Int,
    direction: Direction = Direction.random(),
) : Mob(width, height, direction, maxHealth, PassiveMobBehavior()) {

    override fun changeDir() {
        switchDir()
        controlVector.set(Vector2(direction.basis * speed, 0f))
    }

    override fun damage(damage: Int) {
        super.damage(damage)

        if (damage > 0) {
            if (canJump) {
                jump()
            }
        }
    }
}
