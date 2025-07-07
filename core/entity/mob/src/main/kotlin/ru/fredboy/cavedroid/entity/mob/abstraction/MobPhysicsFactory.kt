package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.physics.box2d.Body
import ru.fredboy.cavedroid.entity.mob.model.Mob

interface MobPhysicsFactory {

    fun createBody(mob: Mob, x: Float, y: Float, physicsCategory: Short): Body
}
