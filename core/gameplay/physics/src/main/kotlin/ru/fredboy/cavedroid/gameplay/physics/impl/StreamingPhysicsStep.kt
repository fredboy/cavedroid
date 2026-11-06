package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.EdgeShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ObjectSet
import ru.fredboy.cavedroid.common.utils.floorDiv
import ru.fredboy.cavedroid.common.utils.floorToInt
import ru.fredboy.cavedroid.game.world.generator.ChunkGenerator
import kotlin.math.max

/**
 * Protects every active actor, including offscreen drops and projectiles, at each fixed step.
 * Missing terrain pauses only the affected bodies. Their velocity and active state are restored
 * afterwards, allowing the next step to retry without generating terrain outside the streaming window.
 */
internal class StreamingPhysicsStep {
    private val bodies = Array<Body>()
    private val actors = Array<Body>()
    private val suspended = Array<Body>()
    private val checked = ObjectSet<Body>()
    private val vertex = Vector2()
    private val center = Vector2()

    fun step(
        world: World,
        delta: Float,
        velocityIterations: Int,
        positionIterations: Int,
        prepareColumn: (Int) -> Boolean,
    ) {
        try {
            // Completing collision preparation can restore additional entities. Include those in
            // this step too. Filter before callbacks, which may destroy/recycle static Body handles.
            do {
                actors.clear()
                world.getBodies(bodies)
                for (body in bodies) {
                    if (body.isActive && body.type != BodyDef.BodyType.StaticBody && checked.add(body)) {
                        actors.add(body)
                    }
                }
                for (body in actors) {
                    val radius = sweepRadius(body)
                    val x = body.worldCenter.x
                    val first = (x - radius).floorToInt() floorDiv ChunkGenerator.CHUNK_W
                    val last = (x + radius).floorToInt() floorDiv ChunkGenerator.CHUNK_W
                    var ready = true
                    for (chunkX in first..last) {
                        if (!prepareColumn(chunkX)) ready = false
                    }
                    if (!ready) {
                        suspended.add(body)
                        body.isActive = false
                    }
                }
            } while (actors.notEmpty())
            world.step(delta, velocityIterations, positionIterations)
        } finally {
            for (body in suspended) body.isActive = true
            suspended.clear()
            checked.clear()
            actors.clear()
            bodies.clear()
        }
    }

    private fun sweepRadius(body: Body): Float {
        center.set(body.localCenter)
        var radius = 0f
        for (fixture in body.fixtureList) {
            val shape = fixture.shape
            var extent = 0f
            when (shape) {
                is CircleShape -> extent = shape.position.dst(center)
                is PolygonShape -> for (i in 0 until shape.vertexCount) {
                    shape.getVertex(i, vertex)
                    extent = max(extent, vertex.dst(center))
                }
                is ChainShape -> for (i in 0 until shape.vertexCount) {
                    shape.getVertex(i, vertex)
                    extent = max(extent, vertex.dst(center))
                }
                is EdgeShape -> {
                    shape.getVertex1(vertex)
                    extent = vertex.dst(center)
                    shape.getVertex2(vertex)
                    extent = max(extent, vertex.dst(center))
                }
            }
            radius = max(radius, extent + shape.radius)
        }
        // Box2D caps translation at 2 metres per step, including acceleration and impulses.
        // A disk about the centre of mass covers rotation of offset fixtures as well.
        return radius + MAX_STEP_TRANSLATION + CONTACT_MARGIN
    }

    private companion object {
        const val MAX_STEP_TRANSLATION = 2f
        const val CONTACT_MARGIN = 0.1f
    }
}
