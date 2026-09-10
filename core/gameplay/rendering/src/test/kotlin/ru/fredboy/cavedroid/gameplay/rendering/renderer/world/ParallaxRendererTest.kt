package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import ru.fredboy.cavedroid.domain.assets.usecase.GetTextureRegionByNameUseCase
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.entity.mob.abstraction.PlayerAdapter
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig

class ParallaxRendererTest {

    private val textureRegions = mockk<GetTextureRegionByNameUseCase>()
    private val gameWorld = mockk<GameWorld>()
    private val player = mockk<PlayerAdapter>()
    private val batch = mockk<SpriteBatch>()
    private val shapes = mockk<ShapeRenderer>()
    private val draws = mutableListOf<Pair<Biome, Float>>()
    private val verticesDrawn = mutableListOf<FloatArray>()
    private val weights = mutableMapOf<Biome, Float>()

    private fun renderer(): ParallaxRenderer {
        val textures = Biome.entries.associateWith {
            mockk<Texture> {
                every { width } returns 4096
                every { height } returns 582
            }
        }
        for ((biome, texture) in textures) {
            every { textureRegions[biome.name.lowercase()] } returns TextureRegion(texture).apply {
                flip(false, true)
            }
        }
        every { player.x } returns 42.5f
        every { gameWorld.generatorConfig } returns WorldGeneratorConfig.getDefault(seed = 1L)
        every { gameWorld.biomeProximityFactor(any(), any(), any()) } answers {
            val predicate = thirdArg<(Biome) -> Boolean>()
            weights[Biome.entries.single(predicate)] ?: 0f
        }
        every { batch.draw(any<Texture>(), any<FloatArray>(), any<Int>(), any<Int>()) } answers {
            val biome = textures.entries.single { it.value === firstArg<Texture>() }.key
            val vertices = secondArg<FloatArray>()
            verticesDrawn += vertices.copyOf()
            val color = Color()
            Color.abgr8888ToColor(color, vertices[thirdArg<Int>() + SpriteBatch.C1])
            draws += biome to color.a
        }
        return ParallaxRenderer(textureRegions, gameWorld, player)
    }

    private fun draw(renderer: ParallaxRenderer): Map<Biome, Float> {
        draws.clear()
        verticesDrawn.clear()
        renderer.draw(batch, shapes, Rectangle(0f, 100f, 40f, 24f), 1f / 60f)
        return draws.toMap()
    }

    private fun contributions(alphas: Map<Biome, Float>): Map<Biome, Float> {
        val result = mutableMapOf<Biome, Float>()
        for ((biome, alpha) in alphas) {
            result.replaceAll { _, value -> value * (1f - alpha) }
            result[biome] = alpha
        }
        return result
    }

    @Test
    fun `only the current biome draws away from boundaries`() {
        val renderer = renderer()
        for (biome in Biome.entries) {
            weights.clear()
            weights[biome] = 1f
            val alphas = draw(renderer)
            assertEquals(setOf(biome), alphas.keys)
            assertEquals(1f, alphas.getValue(biome), 0.01f)
            assertEquals(1, draws.size)
        }
    }

    @Test
    fun `every biome pair blends evenly at a boundary without exposing the sky`() {
        val renderer = renderer()
        for (pair in listOf(Biome.PLAINS to Biome.DESERT, Biome.PLAINS to Biome.WINTER, Biome.DESERT to Biome.WINTER)) {
            weights.clear()
            weights[pair.first] = 1f
            weights[pair.second] = 1f
            val contributions = contributions(draw(renderer))
            assertEquals(0.5f, contributions.getValue(pair.first), 0.01f)
            assertEquals(0.5f, contributions.getValue(pair.second), 0.01f)
            assertEquals(1f, contributions.values.sum(), 0.01f)
        }
    }

    @Test
    fun `three nearby biomes retain their relative weights`() {
        val renderer = renderer()
        weights.putAll(mapOf(Biome.PLAINS to 1f, Biome.DESERT to 0.5f, Biome.WINTER to 0.25f))
        val contributions = contributions(draw(renderer))
        for ((biome, weight) in weights) {
            assertEquals(weight / 1.75f, contributions.getValue(biome), 0.01f)
        }
        assertEquals(1f, contributions.values.sum(), 0.01f)
    }

    @Test
    fun `approaching and leaving a biome reverses the blend without retained opacity`() {
        val renderer = renderer()
        weights[Biome.PLAINS] = 1f
        var previous = 0f
        for (proximity in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
            weights[Biome.DESERT] = proximity
            val contributions = contributions(draw(renderer))
            val desert = contributions[Biome.DESERT] ?: 0f
            assertTrue(desert >= previous)
            previous = desert
        }
        weights[Biome.DESERT] = 0f
        assertEquals(setOf(Biome.PLAINS), draw(renderer).keys)
        verify { gameWorld.biomeProximityFactor(42.5f, 16f, any()) }
    }

    @Test
    fun `unmirrored tiles cover wide viewports across positive and negative repeat boundaries`() {
        val renderer = renderer()
        weights[Biome.PLAINS] = 1f
        for (cameraX in listOf(-1000000f, -256.25f, -256f, -0.25f, 0f, 0.25f, 256f, 256.25f, 1000000f)) {
            verticesDrawn.clear()
            val viewport = Rectangle(cameraX, 100f, 400f, 24f)
            renderer.draw(batch, shapes, viewport, 1f / 60f)

            assertTrue(verticesDrawn.first()[SpriteBatch.X1] <= viewport.x)
            assertTrue(verticesDrawn.last()[SpriteBatch.X3] >= viewport.x + viewport.width)
            for (vertices in verticesDrawn) {
                assertTrue(vertices[SpriteBatch.U1] < vertices[SpriteBatch.U3])
                assertEquals(128f, vertices[SpriteBatch.X3] - vertices[SpriteBatch.X1], 0.01f)
            }
            for ((left, right) in verticesDrawn.zipWithNext()) {
                assertEquals(left[SpriteBatch.X3], right[SpriteBatch.X1], 0.01f)
            }
        }
    }
}
