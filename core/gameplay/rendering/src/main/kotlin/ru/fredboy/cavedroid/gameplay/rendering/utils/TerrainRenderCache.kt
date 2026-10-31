package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import javax.inject.Inject

/** All cache GL resources belong to the game session, never to an individual terrain tile. */
@GameScope
class TerrainRenderCache @Inject constructor() : Disposable {
    private val spriteResource = lazy { RenderingTool.SpriteBatch() }
    private val shapeResource = lazy { RenderingTool.ShapeRenderer() }
    val sprites get() = spriteResource.value
    val shapes get() = shapeResource.value
    private val queue = BudgetedWorkQueue()
    private val free = ArrayDeque<FrameBuffer>()
    private var allocated = 0

    internal fun schedule(work: BudgetedWorkQueue.Work) = queue.add(work)
    internal fun cancel(work: BudgetedWorkQueue.Work) = queue.remove(work)

    var updates = 0L
        private set

    fun prepare(finish: Boolean) {
        updates = queue.run(if (finish) Long.MAX_VALUE else 2_000_000L)
    }

    internal fun acquire(): FrameBuffer = free.removeFirstOrNull() ?: FrameBuffer(
        Pixmap.Format.RGBA8888,
        ChunkFrameBuffer.CHUNK_SIZE * PIXELS_PER_METER.toInt(),
        ChunkFrameBuffer.CHUNK_SIZE * PIXELS_PER_METER.toInt(),
        false,
    ).also { allocated++ }

    internal fun release(buffer: FrameBuffer) {
        free.addLast(buffer)
    }

    /** The viewport's retained images plus a staging buffer bound the GPU resource pool. */
    fun trim(capacity: Int) {
        while (allocated > capacity && free.isNotEmpty()) {
            free.removeFirst().dispose()
            allocated--
        }
    }

    override fun dispose() {
        queue.clear()
        free.forEach { it.dispose() }
        free.clear()
        allocated = 0
        if (spriteResource.isInitialized()) sprites.dispose()
        if (shapeResource.isInitialized()) shapes.dispose()
    }
}
