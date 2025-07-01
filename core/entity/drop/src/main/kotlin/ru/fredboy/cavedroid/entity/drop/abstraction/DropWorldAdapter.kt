package ru.fredboy.cavedroid.entity.drop.abstraction

import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener

interface DropWorldAdapter {

    fun addOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun addOnBlockPlacedListener(listener: OnBlockPlacedListener)

    fun removeOnBlockDestroyedListener(listener: OnBlockDestroyedListener)

    fun removeOnBlockPlacedListener(listener: OnBlockPlacedListener)
}
