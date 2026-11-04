package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import javax.inject.Inject

/** Hit areas published by tutorial renderers, in HUD viewport coordinates. */
@GameScope
class TutorialTapTargets @Inject constructor() {
    private data class Target(val bounds: Rectangle, val dismiss: () -> Boolean)
    private val targets = linkedMapOf<String, Target>()

    fun register(key: String, bounds: Rectangle, dismiss: () -> Boolean) {
        targets[key] = Target(Rectangle(bounds), dismiss)
    }

    fun clear(key: String) {
        targets.remove(key)
    }

    fun dismissAt(x: Float, y: Float): Boolean = targets.values.toList().asReversed().any {
        it.bounds.contains(x, y) && it.dismiss()
    }
}
