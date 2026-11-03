package ru.fredboy.cavedroid.entity.container.abstraction

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import kotlin.reflect.KClass

interface ContainerWorldAdapter {

    fun containerX(x: Int): Int = x

    fun chestBlockKey(x: Int, y: Int, z: Int): String?

    /** Change chest appearance without destroying or recreating its container. */
    fun setChestBlockKey(x: Int, y: Int, z: Int, key: String)

    fun checkContainerAtCoordinates(
        coordinates: ContainerCoordinates,
        requiredType: KClass<out Block.Container>,
    ): Boolean

    fun isContainerChunkLoaded(coordinates: ContainerCoordinates): Boolean

    fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun addOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun createFurnaceLight(x: Float, y: Float): LightHandle
}
