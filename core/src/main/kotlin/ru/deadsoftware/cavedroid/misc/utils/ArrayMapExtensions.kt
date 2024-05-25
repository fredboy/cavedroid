package ru.deadsoftware.cavedroid.misc.utils

import com.badlogic.gdx.utils.ObjectMap

object ArrayMapExtensions {
    operator fun <K, V> ObjectMap.Entry<K, V>.component1(): K = this.key

    operator fun <K, V> ObjectMap.Entry<K, V>.component2(): V = this.value
}
