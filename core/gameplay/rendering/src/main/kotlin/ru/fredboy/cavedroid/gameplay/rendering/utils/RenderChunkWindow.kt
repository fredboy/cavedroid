package ru.fredboy.cavedroid.gameplay.rendering.utils

import com.badlogic.gdx.math.Rectangle
import kotlin.math.ceil
import kotlin.math.floor

/** One tile of lookahead, with a position-independent pool limit to avoid reallocating at seams. */
internal class RenderChunkWindow(viewport: Rectangle) {
    val minX = floor(viewport.x / ChunkFrameBuffer.CHUNK_SIZE).toInt()
    val maxX = floor((viewport.x + viewport.width) / ChunkFrameBuffer.CHUNK_SIZE).toInt()
    val minY = floor(viewport.y / ChunkFrameBuffer.CHUNK_SIZE).toInt()
    val maxY = floor((viewport.y + viewport.height) / ChunkFrameBuffer.CHUNK_SIZE).toInt()
    val capacity = (ceil(viewport.width / ChunkFrameBuffer.CHUNK_SIZE).toInt() + 3) *
        (ceil(viewport.height / ChunkFrameBuffer.CHUNK_SIZE).toInt() + 3)

    fun retains(x: Int, y: Int): Boolean = x in (minX - 1)..(maxX + 1) && y in (minY - 1)..(maxY + 1)
}
