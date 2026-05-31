package ru.fredboy.cavedroid.entity.projectile.abstraction

import com.badlogic.gdx.physics.box2d.World

interface ProjectileWorldAdapter {

    @Deprecated("Use start/end")
    val width: Int
    val height: Int

    fun getBox2dWorld(): World
}
