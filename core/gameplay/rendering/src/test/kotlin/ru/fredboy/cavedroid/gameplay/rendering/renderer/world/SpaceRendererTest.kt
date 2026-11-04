package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.Color
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

class SpaceRendererTest {

    private val world = mockk<GameWorld> {
        every { height } returns 256
    }
    private val batch = mockk<SpriteBatch>(relaxed = true)
    private val shapes = mockk<ShapeRenderer>(relaxed = true)
    private val rectangles = mutableListOf<Rectangle>()
    private val gradients = mutableListOf<Gradient>()
    private val renderer = SpaceRenderer(world)

    init {
        every { shapes.rect(any<Float>(), any<Float>(), any<Float>(), any<Float>()) } answers {
            rectangles += Rectangle(firstArg(), secondArg(), thirdArg(), arg(3))
        }
        every {
            shapes.rect(
                any<Float>(),
                any<Float>(),
                any<Float>(),
                any<Float>(),
                any<Color>(),
                any<Color>(),
                any<Color>(),
                any<Color>(),
            )
        } answers {
            gradients += Gradient(
                Rectangle(firstArg(), secondArg(), thirdArg(), arg(3)),
                (4..7).map { Color(arg<Color>(it)) },
            )
        }
    }

    @Test
    fun `space does not draw inside the world including the bottom bedrock row`() {
        renderer.draw(batch, shapes, Rectangle(0f, 240f, 40f, 16f), 0f)

        assertTrue(rectangles.isEmpty())
        assertTrue(gradients.isEmpty())
        verify(exactly = 0) { batch.end() }
    }

    @Test
    fun `space background and fade start at the underside of bedrock`() {
        renderer.draw(batch, shapes, Rectangle(10f, 250f, 40f, 24f), 0f)

        assertEquals(listOf(Rectangle(10f, 256f, 40f, 18f)), rectangles)
        val gradient = gradients.single()
        assertEquals(Rectangle(10f, 256f, 40f, 32f), gradient.bounds)
        val borderColor = Color.valueOf("100d0c")
        assertEquals(listOf(borderColor, borderColor, Color.BLACK, Color.BLACK), gradient.colors)
    }

    @Test
    fun `space fade is anchored to world depth without depending on sunlight or weather`() {
        // The world mock supplies no time, sunlight, or weather values.
        renderer.draw(batch, shapes, Rectangle(0f, 272f, 40f, 24f), 0f)

        assertEquals(listOf(Rectangle(0f, 272f, 40f, 24f)), rectangles)
        val gradient = gradients.single()
        assertEquals(Rectangle(0f, 272f, 40f, 16f), gradient.bounds)
        val halfwayColor = Color(8f / 255f, 6.5f / 255f, 6f / 255f, 1f)
        assertEquals(listOf(halfwayColor, halfwayColor, Color.BLACK, Color.BLACK), gradient.colors)
    }

    @Test
    fun `space at and below the end of the fade is solid black`() {
        renderer.draw(batch, shapes, Rectangle(0f, 288f, 40f, 24f), 0f)

        assertEquals(listOf(Rectangle(0f, 288f, 40f, 24f)), rectangles)
        assertTrue(gradients.isEmpty())
        verify { shapes.setColor(Color.BLACK) }
    }

    private data class Gradient(
        val bounds: Rectangle,
        val colors: List<Color>,
    )
}
