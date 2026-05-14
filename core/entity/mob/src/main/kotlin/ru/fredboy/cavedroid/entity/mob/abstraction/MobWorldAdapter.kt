package ru.fredboy.cavedroid.entity.mob.abstraction

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle

interface MobWorldAdapter {

    val height: Int

    val width: Int

    fun setForegroundBlock(x: Int, y: Int, block: Block)

    fun setBackgroundBlock(x: Int, y: Int, block: Block)

    fun getForegroundBlock(x: Int, y: Int): Block

    fun getBackgroundBlock(x: Int, y: Int): Block

    fun destroyForegroundBlock(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean)

    fun destroyBackgroundBlock(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean)

    fun findSpawnPoint(): Vector2

    fun getBox2dWorld(): World

    fun getClimbable(hitbox: Rectangle): Block.Climbable?

    fun createPlayerSightLight(body: Body, x: Float, y: Float): LightHandle

    fun isDayTime(): Boolean

    fun canPlaceToForeground(x: Int, y: Int, value: Block): Boolean
}
