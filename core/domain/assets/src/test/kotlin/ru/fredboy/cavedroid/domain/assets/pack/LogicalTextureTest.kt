package ru.fredboy.cavedroid.domain.assets.pack

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.TextureData
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Custom texture data does no native upload; only the GL interface is stubbed. */
class LogicalTextureTest {
    private val previousGl = Gdx.gl
    private val previousGraphics = Gdx.graphics

    @BeforeEach
    fun setup() {
        Gdx.gl = mockk<GL20>(relaxed = true)
        Gdx.graphics = mockk(relaxed = true)
    }

    @AfterEach
    fun cleanup() {
        Gdx.gl = previousGl
        Gdx.graphics = previousGraphics
    }

    private fun texture(width: Int, height: Int, scale: Int): LogicalTexture {
        val data = mockk<TextureData>(relaxed = true) {
            every { getType() } returns TextureData.TextureDataType.Custom
            every { isPrepared() } returns true
            every { getWidth() } returns width
            every { getHeight() } returns height
            every { isManaged() } returns false
        }
        return LogicalTexture(data, scale)
    }

    @Test
    fun `animation frames preserve UV extent and logical size at higher resolutions`() {
        val texture = texture(64, 640, 4)
        val frame = Sprite(texture, 0, 16 * 3, 16, 16)
        assertEquals(64, texture.sourceWidth)
        assertEquals(640, texture.sourceHeight)
        assertEquals(16, texture.width)
        assertEquals(160, texture.height)
        assertEquals(16, frame.regionWidth)
        assertEquals(16f, frame.width)
        assertEquals(0.3f, frame.v, 0.00001f)
        assertEquals(0.4f, frame.v2, 0.00001f)
        frame.flip(false, true)
        assertEquals(0.3f, frame.v2, 0.00001f)
    }

    @Test
    fun `cropping and copying regions retains logical margins`() {
        val texture = texture(32, 32, 2)
        val slab = Sprite(texture, 0, 8, 16, 8)
        val copy = TextureRegion(slab)
        assertEquals(16, copy.regionWidth)
        assertEquals(8, copy.regionHeight)
        assertEquals(0.5f, copy.v)
        assertEquals(1f, copy.v2)
    }

    @Test
    fun `GUI and mob sprites retain their original display dimensions`() {
        val inventory = TextureRegion(texture(352, 332, 2))
        val hand = Sprite(texture(16, 48, 4))
        assertEquals(176, inventory.regionWidth)
        assertEquals(166, inventory.regionHeight)
        assertEquals(4f, hand.width)
        assertEquals(12f, hand.height)
    }
}
