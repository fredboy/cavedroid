package ru.fredboy.cavedroid.gameplay.physics.impl

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.GdxNativesLoader
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Exercises actual stepping and activation semantics with Box2D natives, without a GL context. */
class StreamingPhysicsStepTest {
    private lateinit var world: World
    private val gate = StreamingPhysicsStep()

    @BeforeEach
    fun createWorld() {
        GdxNativesLoader.load()
        Box2D.init()
        world = World(Vector2.Zero, false)
    }

    @AfterEach
    fun disposeWorld() {
        world.dispose()
    }

    @Test
    fun `offscreen terrain is installed before an actor can fall through it`() {
        val actor = actor(1015f, -1f)
        actor.setLinearVelocity(0f, 15f)
        val prepared = mutableSetOf<Int>()
        repeat(30) {
            step { chunk ->
                if (prepared.add(chunk) && chunk == 63) {
                    val ground = world.createBody(BodyDef().apply { position.set(1015f, 0.5f) })
                    val shape = PolygonShape()
                    shape.setAsBox(1f, 0.5f)
                    ground.createFixture(shape, 0f)
                    shape.dispose()
                }
                true
            }
        }
        assertTrue(63 in prepared)
        assertTrue(actor.position.y < 0f)
    }

    @Test
    fun `missing neighbour pauses only affected actor and resumes with velocity intact`() {
        val waiting = actor(15f)
        val safe = actor(8f)
        waiting.setLinearVelocity(30f, 0f)
        safe.setLinearVelocity(6f, 0f)
        step { it == 0 }
        assertEquals(15f, waiting.position.x)
        assertEquals(30f, waiting.linearVelocity.x)
        assertTrue(waiting.isActive)
        assertTrue(safe.position.x > 8f)
        step { true }
        assertTrue(waiting.position.x > 15f)
    }

    @Test
    fun `negative boundary uses floor division and cannot be crossed while missing`() {
        val body = actor(0.5f)
        body.setLinearVelocity(-120f, 0f)
        val requested = mutableSetOf<Int>()
        step {
            requested.add(it)
            it >= 0
        }
        assertEquals(setOf(-1, 0), requested)
        assertEquals(0.5f, body.position.x)
    }

    @Test
    fun `each substep rechecks bodies approaching a chunk boundary`() {
        val body = actor(10f)
        body.setLinearVelocity(120f, 0f)
        repeat(10) { step { it == 0 } }
        assertEquals(14f, body.position.x, 0.001f)
    }

    @Test
    fun `entities restored by readiness callbacks are protected in the same step`() {
        actor(8f)
        var restored: Body? = null
        step { chunk ->
            if (chunk == 0 && restored == null) {
                restored = actor(31f).apply { setLinearVelocity(120f, 0f) }
            }
            chunk != 2
        }
        assertEquals(31f, requireNotNull(restored).position.x)
        assertTrue(requireNotNull(restored).isActive)
    }

    @Test
    fun `inactive bodies remain inactive and do not force preparation`() {
        val body = actor(15f)
        body.isActive = false
        var calls = 0
        step {
            calls++
            true
        }
        assertEquals(0, calls)
        assertFalse(body.isActive)
    }

    @Test
    fun `suspended bodies are restored even when preparation fails`() {
        val first = actor(15f)
        val second = actor(47f)
        var calls = 0
        assertThrows(IllegalStateException::class.java) {
            step {
                if (++calls > 2) error("load failed")
                false
            }
        }
        assertTrue(first.isActive)
        assertTrue(second.isActive)
        step { true }
    }

    @Test
    fun `rotating offset fixtures reserve their entire sweep`() {
        val body = actor(8f)
        val shape = PolygonShape()
        shape.setAsBox(0.5f, 0.5f, Vector2(0f, 8f), 0f)
        body.createFixture(shape, 0f)
        shape.dispose()
        val requested = mutableSetOf<Int>()
        step {
            requested.add(it)
            true
        }
        assertEquals(setOf(-1, 0, 1), requested)
    }

    @Test
    fun `forces and kinematic motion cannot bypass a missing chunk`() {
        val dynamic = actor(15f)
        dynamic.applyForceToCenter(100000f, 0f, true)
        val kinematic = actor(-15f)
        kinematic.type = BodyDef.BodyType.KinematicBody
        kinematic.setLinearVelocity(-1000f, 0f)
        step { it == 0 || it == -1 }
        assertEquals(15f, dynamic.position.x)
        assertEquals(-15f, kinematic.position.x)
    }

    private fun step(prepare: (Int) -> Boolean) {
        gate.step(world, 1f / 60f, 6, 2, prepare)
    }

    private fun actor(x: Float, y: Float = 0f): Body {
        val body = world.createBody(
            BodyDef().apply {
                type = BodyDef.BodyType.DynamicBody
                position.set(x, y)
            },
        )
        val shape = CircleShape()
        shape.radius = 0.25f
        body.createFixture(shape, 1f)
        shape.dispose()
        return body
    }
}
