package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.container.FurnaceStateChangedListener
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.store.ChunkListener

/** A published image and at most one in-progress replacement. Shared GL tools belong to [cache]. */
class ChunkFrameBuffer<Renderer : RenderingTool>(
    private val chunkX: Int,
    private val chunkY: Int,
    private val gameWorld: GameWorld,
    private val containerController: ContainerController,
    private val renderingTool: Renderer,
    private val cache: TerrainRenderCache,
) : Disposable,
    BudgetedWorkQueue.Work {
    private var front: FrameBuffer? = null
    private var staging: FrameBuffer? = null
    private var region: TextureRegion? = null
    private val revisions = CacheRevision()
    private var buildingRevision = -1L
    private var progress = 0
    private var draw: ((Renderer, Int, Int, Float, Float) -> Unit)? = null
    private val projection = Matrix4().setToOrtho2D(0f, 0f, CHUNK_SIZE.toFloat(), CHUNK_SIZE.toFloat())
    private var patchMinX = CHUNK_SIZE
    private var patchMinY = CHUNK_SIZE
    private var patchMaxX = -1
    private var patchMaxY = -1
    private var fullInvalidation = true

    override var priority: Int = 0
        private set
    val ready get() = front != null
    val dirty get() = revisions.dirty

    private val onBlockPlacedListener = OnBlockPlacedListener { _, x, y, _ -> invalidateRegion(x, y) }
    private val furnaceStateChangedListener = FurnaceStateChangedListener { x, y, _, _ -> invalidateRegion(x, y) }
    private val chunkListener = object : ChunkListener {
        override fun onChunkLoaded(chunkX: Int) = invalidateColumn(chunkX)
        override fun onChunkUnloaded(chunkX: Int) = invalidateColumn(chunkX)
    }

    init {
        gameWorld.addBlockPlacedListener(onBlockPlacedListener)
        containerController.addFurnaceListener(furnaceStateChangedListener)
        gameWorld.addChunkListener(chunkListener)
    }

    private fun invalidateColumn(column: Int) {
        if (column != chunkX) return
        revisions.invalidate()
        fullInvalidation = true
    }

    private fun invalidateRegion(x: Int, y: Int) {
        val localY = y - chunkY * CHUNK_SIZE
        if (localY !in -1..CHUNK_SIZE) return
        var localX = x - chunkX * CHUNK_SIZE
        if (!gameWorld.isInfinite && localX !in -1..CHUNK_SIZE) {
            val width = gameWorld.width
            localX -= kotlin.math.round(localX.toFloat() / width).toInt() * width
        }
        if (localX !in -1..CHUNK_SIZE) return
        revisions.invalidate()
        patchMinX = minOf(patchMinX, (localX - 1).coerceAtLeast(0))
        patchMinY = minOf(patchMinY, (localY - 1).coerceAtLeast(0))
        patchMaxX = maxOf(patchMaxX, (localX + 1).coerceAtMost(CHUNK_SIZE - 1))
        patchMaxY = maxOf(patchMaxY, (localY + 1).coerceAtMost(CHUNK_SIZE - 1))
    }

    fun prepare(priority: Int, drawFunction: (Renderer, Int, Int, Float, Float) -> Unit) {
        this.priority = if (dirty && ready) -1000 else priority
        draw = drawFunction
        if (dirty) cache.schedule(this)
    }

    override fun step(deadline: Long): Boolean {
        if (!dirty) return true
        val drawFunction = requireNotNull(draw)
        if (buildingRevision != revisions.version) {
            staging?.let(cache::release)
            staging = null
            progress = 0
            buildingRevision = revisions.version
        }
        val area = (patchMaxX - patchMinX + 1) * (patchMaxY - patchMinY + 1)
        if (!fullInvalidation && front != null && area in 1..25) {
            redrawRegion(requireNotNull(front), drawFunction)
            return finishRevision()
        }
        val target = staging ?: cache.acquire().also { staging = it }
        target.begin()
        try {
            if (progress == 0) clear()
            renderingTool.setProjectionMatrix(projection)
            enableBlending()
            renderingTool.begin()
            try {
                val end = minOf(progress + 8, CHUNK_SIZE * CHUNK_SIZE)
                while (progress < end) {
                    drawCell(drawFunction, progress / CHUNK_SIZE, progress % CHUNK_SIZE)
                    progress++
                }
            } finally {
                renderingTool.end()
            }
        } finally {
            target.end()
        }
        if (progress != CHUNK_SIZE * CHUNK_SIZE || buildingRevision != revisions.version) return false
        front?.let(cache::release)
        front = target
        staging = null
        region = TextureRegion(target.colorBufferTexture).apply {
            flip(false, true)
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest)
        }
        finishRevision()
        return true
    }

    private fun finishRevision(): Boolean {
        if (!revisions.publish(buildingRevision)) return false
        fullInvalidation = false
        patchMinX = CHUNK_SIZE
        patchMinY = CHUNK_SIZE
        patchMaxX = -1
        patchMaxY = -1
        return true
    }

    private fun redrawRegion(target: FrameBuffer, drawFunction: (Renderer, Int, Int, Float, Float) -> Unit) {
        target.begin()
        try {
            val pixels = PIXELS_PER_METER.toInt()
            Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST)
            Gdx.gl.glScissor(patchMinX * pixels, patchMinY * pixels, (patchMaxX - patchMinX + 1) * pixels, (patchMaxY - patchMinY + 1) * pixels)
            clear()
            renderingTool.setProjectionMatrix(projection)
            enableBlending()
            renderingTool.begin()
            try {
                // Include sources outside the scissor whose sprites extend into the changed region.
                for (x in (patchMinX - 1).coerceAtLeast(0)..(patchMaxX + 1).coerceAtMost(CHUNK_SIZE - 1)) {
                    for (y in (patchMinY - 1).coerceAtLeast(0)..(patchMaxY + 1).coerceAtMost(CHUNK_SIZE - 1)) drawCell(drawFunction, x, y)
                }
            } finally {
                renderingTool.end()
            }
        } finally {
            Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST)
            target.end()
        }
    }

    private fun enableBlending() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun clear() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    private fun drawCell(drawFunction: (Renderer, Int, Int, Float, Float) -> Unit, x: Int, y: Int) {
        drawFunction(renderingTool, chunkX * CHUNK_SIZE + x, chunkY * CHUNK_SIZE + y, x.toFloat(), y.toFloat())
    }

    fun render(spriteBatch: SpriteBatch) {
        region?.let { spriteBatch.draw(it, chunkX * CHUNK_SIZE.toFloat(), chunkY * CHUNK_SIZE.toFloat(), CHUNK_SIZE.toFloat(), CHUNK_SIZE.toFloat()) }
    }

    override fun dispose() {
        cache.cancel(this)
        front?.let(cache::release)
        staging?.let(cache::release)
        front = null
        staging = null
        region = null
        gameWorld.removeBlockPlacedListener(onBlockPlacedListener)
        containerController.removeFurnaceListener(furnaceStateChangedListener)
        gameWorld.removeChunkListener(chunkListener)
    }

    companion object {
        const val CHUNK_SIZE = 16
    }
}
