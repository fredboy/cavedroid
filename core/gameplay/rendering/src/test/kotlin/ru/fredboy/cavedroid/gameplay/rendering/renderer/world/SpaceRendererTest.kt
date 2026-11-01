package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class SpaceRendererTest {

    private val world = mockk<GameWorld> {
        every { height } returns 256
        every { generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 1L)
    }
    private val batch = mockk<SpriteBatch>(relaxed = true)
    private val shapes = mockk<ShapeRenderer>(relaxed = true)
    private val rectangles = mutableListOf<Rectangle>()
    private val renderer = SpaceRenderer(world)

    init {
        every { shapes.rect(any<Float>(), any<Float>(), any<Float>(), any<Float>()) } answers {
            rectangles += Rectangle(firstArg(), secondArg(), thirdArg(), arg(3))
        }
    }

    @Test
    fun `space does not draw inside the world including the bottom bedrock row`() {
        renderer.draw(batch, shapes, Rectangle(0f, 240f, 40f, 16f), 0f)

        assertTrue(rectangles.isEmpty())
        verify(exactly = 0) { batch.end() }
    }

    @Test
    fun `space and stars are clipped to the underside of bedrock`() {
        renderer.draw(batch, shapes, Rectangle(10f, 250f, 40f, 24f), 0f)

        assertEquals(Rectangle(10f, 256f, 40f, 18f), rectangles.first())
        assertTrue(rectangles.size > 1)
        assertTrue(rectangles.all { it.y >= 256f && it.y + it.height <= 274f })
        assertTrue(rectangles.all { it.x >= 10f && it.x + it.width <= 50f })
    }

    @Test
    fun `space remains starry without depending on sunlight or weather`() {
        // The world mock supplies no time, sunlight, or weather values.
        renderer.draw(batch, shapes, Rectangle(0f, 270f, 40f, 24f), 0f)

        assertEquals(Rectangle(0f, 270f, 40f, 24f), rectangles.first())
        assertEquals(161, rectangles.size)
        verify { shapes.setColor(0f, 0f, 0.1f, 1f) }
    }
}
