package ru.fredboy.cavedroid.common.utils

import com.badlogic.gdx.math.Vector2

class Vector2Proxy(
    private val getVelocity: () -> Vector2,
    private val setVelocity: (Vector2) -> Unit,
) {

    var x: Float
        get() = getVelocity().x
        set(value) {
            val v = getVelocity()
            setVelocity(Vector2(value, v.y))
        }

    var y: Float
        get() = getVelocity().y
        set(value) {
            val v = getVelocity()
            setVelocity(Vector2(v.x, value))
        }

    val isZero get() = get().isZero

    fun set(x: Float, y: Float) {
        setVelocity(Vector2(x, y))
    }

    fun set(v: Vector2) {
        setVelocity(v)
    }

    fun get(): Vector2 = getVelocity()

    override fun toString(): String {
        return get().toString()
    }

    override fun equals(other: Any?): Boolean {
        return get() == other
    }

    override fun hashCode(): Int {
        return get().hashCode()
    }
}
