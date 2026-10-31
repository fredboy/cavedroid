package ru.fredboy.cavedroid.game.world.presentation

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Array
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WorldPoseHistoryTest {
    private val bodies = Array<Body>()
    private val body = mockk<Body>()
    private var owner: Any = Any()
    private val position = Vector2()
    private val history = WorldPoseHistory()
    private val output = Vector2()

    init {
        every { body.userData } answers { owner }
        every { body.position } answers { position }
        every { body.linearVelocity } returns Vector2(1f, 0f)
        bodies.add(body)
    }

    @Test
    fun `only registered presentation bodies are sampled and later mutations snap`() {
        history.sample(body, 0f, 0f, output)
        history.beforeStep(bodies)
        position.x = 1f
        history.afterStep(bodies, 0f)
        assertEquals(0.5f, history.sample(body, 0.5f, 0f, output).x)
        position.x = 100f
        assertEquals(100f, history.sample(body, 0.5f, 0f, output).x)
        assertEquals(100f, position.x)
    }

    @Test
    fun `recycled native wrappers cannot inherit a previous actors pose`() {
        history.sample(body, 0f, 0f, output)
        history.beforeStep(bodies)
        position.x = 1f
        history.afterStep(bodies, 0f)
        owner = Any()
        position.x = 100f
        assertEquals(100f, history.sample(body, 0.5f, 0f, output).x)
    }

    @Test
    fun `destroyed actors are forgotten and discontinuous physics hooks snap`() {
        history.sample(body, 0f, 0f, output)
        history.beforeStep(bodies)
        position.x = 50f
        history.afterStep(bodies, 0f)
        assertEquals(50f, history.sample(body, 0.5f, 0f, output).x)
        bodies.clear()
        history.beforeStep(bodies)
        bodies.add(body)
        position.x = 100f
        assertEquals(100f, history.sample(body, 0.5f, 0f, output).x)
    }
}
