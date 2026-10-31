package ru.fredboy.cavedroid.game.world.presentation

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array

/** Captured after every substep's gameplay hooks, including passenger synchronization. */
internal class WorldPoseHistory {
    private class Entry(val owner: Any?, val pose: PoseHistory = PoseHistory(), var generation: Long = 0)
    private val entries = mutableMapOf<Body, Entry>()
    private val bodies = Array<Body>()
    private var generation = 0L

    fun beforeStep(world: World) {
        world.getBodies(bodies)
        beforeStep(bodies)
    }

    internal fun beforeStep(liveBodies: Array<Body>) {
        generation++
        for (body in liveBodies) {
            val entry = entries[body] ?: continue
            if (entry.owner !== body.userData) {
                entries.remove(body)
                continue
            }
            entry.generation = generation
            val position = body.position
            entry.pose.beforeStep(position.x, position.y, body.linearVelocity.angleDeg())
        }
        val iterator = entries.iterator()
        while (iterator.hasNext()) if (iterator.next().value.generation != generation) iterator.remove()
    }

    fun afterStep(world: World, worldWidth: Float) {
        world.getBodies(bodies)
        afterStep(bodies, worldWidth)
        bodies.clear()
    }

    internal fun afterStep(liveBodies: Array<Body>, worldWidth: Float) {
        for (body in liveBodies) {
            val entry = entries[body] ?: continue
            if (entry.owner !== body.userData) {
                entries.remove(body)
                continue
            }
            val position = body.position
            if (entry.generation != generation ||
                kotlin.math.abs(PoseHistory.displacement(entry.pose.x, position.x, worldWidth)) > 2.1f ||
                kotlin.math.abs(entry.pose.y - position.y) > 2.1f
            ) {
                entry.pose.reset(position.x, position.y, body.linearVelocity.angleDeg())
            }
            entry.generation = generation
            entry.pose.afterStep(position.x, position.y, body.linearVelocity.angleDeg())
        }
    }

    private fun entry(body: Body): Entry {
        val existing = entries[body]
        if (existing != null && existing.owner === body.userData) return existing
        return Entry(body.userData).also { entries[body] = it }
    }

    fun sample(body: Body, alpha: Float, worldWidth: Float, out: Vector2): Vector2 {
        val position = body.position
        val entry = entry(body)
        if (!entry.pose.matches(position.x, position.y, worldWidth)) entry.pose.reset(position.x, position.y, body.linearVelocity.angleDeg())
        return out.set(entry.pose.sampleX(alpha, worldWidth), entry.pose.sampleY(alpha))
    }

    fun heading(body: Body, alpha: Float): Float {
        val entry = entries[body]
        return if (entry == null || entry.owner !== body.userData) body.linearVelocity.angleDeg() else entry.pose.sampleHeading(alpha)
    }

    fun clear() {
        entries.clear()
        bodies.clear()
    }
}
