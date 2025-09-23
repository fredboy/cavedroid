package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.game.world.GameWorld

class ChunkFrameBuffer<Renderer : RenderingTool>(
    private val chunkX: Int,
    private val chunkY: Int,
    private val gameWorld: GameWorld,
    private val renderingTool: Renderer,
) : Disposable {

    init {
        renderingTool.apply {
            setProjectionMatrix(Matrix4().setToOrtho2D(0f, 0f, CHUNK_SIZE.toFloat(), CHUNK_SIZE.toFloat()))
        }
    }

    private val fbo = FrameBuffer(
        /* format = */ Pixmap.Format.RGBA8888,
        /* width = */ CHUNK_SIZE * PIXELS_PER_METER.toInt(),
        /* height = */ CHUNK_SIZE * PIXELS_PER_METER.toInt(),
        /* hasDepth = */ false,
    )

    private val region = TextureRegion(fbo.colorBufferTexture).apply {
        flip(false, true)
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
    }

    private val chunkWorldX = chunkX * CHUNK_SIZE
    private val chunkWorldY = chunkY * CHUNK_SIZE

    private val onBlockPlacedListener = OnBlockPlacedListener { _, x, y, _ ->
        if (x >= chunkWorldX && x < chunkWorldX + CHUNK_SIZE && y >= chunkWorldY && y < chunkWorldY + CHUNK_SIZE) {
            dirty = true
        }
    }

    var dirty = true

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
    }

    fun rebuild(drawFunction: (Renderer, Int, Int, Float, Float) -> Unit) {
        fbo.begin()
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        renderingTool.begin()

        for (x in 0 until CHUNK_SIZE) {
            for (y in 0 until CHUNK_SIZE) {
                val worldX = chunkX * CHUNK_SIZE + x
                val worldY = chunkY * CHUNK_SIZE + y
                val localX = x.toFloat()
                val localY = y.toFloat()

                drawFunction(renderingTool, worldX, worldY, localX, localY)
            }
        }

        renderingTool.end()
        fbo.end()

        dirty = false
    }

    fun render(
        spriteBatch: SpriteBatch,
        drawFunction: (Renderer, Int, Int, Float, Float) -> Unit,
    ) {
        if (dirty) {
            spriteBatch.end()
            rebuild(drawFunction)
            spriteBatch.begin()
        }

        spriteBatch.draw(
            region,
            chunkX * CHUNK_SIZE.toFloat(),
            chunkY * CHUNK_SIZE.toFloat(),
            CHUNK_SIZE.toFloat(),
            CHUNK_SIZE.toFloat(),
        )
    }

    override fun dispose() {
        renderingTool.dispose()
        fbo.dispose()
        gameWorld.removeBlockPlacedListener(onBlockPlacedListener)
    }

    companion object {
        const val CHUNK_SIZE = 16
    }
}
