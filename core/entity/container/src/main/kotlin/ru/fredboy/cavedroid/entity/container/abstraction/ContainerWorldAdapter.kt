package ru.fredboy.cavedroid.entity.container.abstraction

import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.entity.container.model.ContainerCoordinates
import kotlin.reflect.KClass

interface ContainerWorldAdapter {

    fun checkContainerAtCoordinates(
        coordinates: ContainerCoordinates,
        requiredType: KClass<out Block.Container>
    ): Boolean

    fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun addOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener)

}