package ru.fredboy.cavedroid.entity.drop.abstraction

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener

interface DropWorldAdapter {

    val width: Int

    val height: Int

    fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun addOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun getForegroundBlock(x: Int, y: Int): Block

    fun getBox2dWorld(): World

    fun getMediumLiquid(hitbox: Rectangle): Block.Fluid?
}
