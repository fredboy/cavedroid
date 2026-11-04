package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.WeatherLightning

class LightningRendererTest {
    private val previousGl = Gdx.gl
    private val world = mockk<GameWorld> {
        every { generatorConfig.seaLevel } returns 64
    }
    private val lightning = WeatherLightning().apply { intensity = 1f }
    private val renderer = LightningRenderer(world, lightning)
    private val batch = mockk<SpriteBatch>(relaxed = true)
    private val shapes = mockk<ShapeRenderer>(relaxed = true)
    private val rectangles = mutableListOf<Rectangle>()

    @BeforeEach
    fun setUp() {
        Gdx.gl = mockk<GL20>(relaxed = true)
        every { shapes.rect(any<Float>(), any<Float>(), any<Float>(), any<Float>()) } answers {
            rectangles += Rectangle(firstArg(), secondArg(), thirdArg(), arg(3))
        }
    }

    @AfterEach
    fun tearDown() {
        Gdx.gl = previousGl
    }

    @Test
    fun `a view crossing sea level flashes only above the fixed background`() {
        renderer.draw(batch, shapes, Rectangle(10f, 60f, 40f, 20f), 0f)

        assertEquals(listOf(Rectangle(10f, 60f, 40f, 4f)), rectangles)
    }

    @Test
    fun `a surface view flashes across its visible background`() {
        renderer.draw(batch, shapes, Rectangle(10f, 20f, 40f, 20f), 0f)

        assertEquals(listOf(Rectangle(10f, 20f, 40f, 20f)), rectangles)
    }

    @Test
    fun `an underground view does not flash even when thunder is active`() {
        renderer.draw(batch, shapes, Rectangle(10f, 64f, 40f, 20f), 0f)

        assertEquals(emptyList<Rectangle>(), rectangles)
        verify(exactly = 0) { batch.end() }
    }
}
