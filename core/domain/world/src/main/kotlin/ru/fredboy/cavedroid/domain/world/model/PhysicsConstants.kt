package ru.fredboy.cavedroid.domain.world.model

object PhysicsConstants {
    const val CATEGORY_BLOCK: Short = (1 shl 0).toShort()
    const val CATEGORY_MOB: Short = (1 shl 1).toShort()
    const val CATEGORY_DROP: Short = (1 shl 2).toShort()

    const val CATEGORY_OPAQUE: Short = (1 shl 3).toShort()
}
