package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.math.Vector2

interface ProjectileAdapter {

    fun addProjectile(
        itemKey: String,
        damage: Int,
        dropOnGround: Boolean,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        velocity: Vector2,
    )
}
