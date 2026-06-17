package ru.fredboy.cavedroid.gameplay.lighting.bfs

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.game.world.GameWorld
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class LightingOverlayRenderer(
    private val gameContextRepository: GameContextRepository,
    private val gameWorld: GameWorld,
    private var grid: LightGrid,
) : Disposable {

    private val spriteBatch = SpriteBatch()

    /** Points the overlay at a freshly rebuilt window grid (infinite worlds re-centre the window). */
    fun updateGrid(grid: LightGrid) {
        this.grid = grid
    }

    private var pixmap: Pixmap? = null
    private var texture: Texture? = null
    private var region: TextureRegion? = null
    private var pixmapWidth: Int = 0
    private var pixmapHeight: Int = 0

    private var rawBrightness: FloatArray = FloatArray(0)
    private var rawCapacity: Int = 0

    fun render(camera: OrthographicCamera, sunLight: Float) {
        val sunBrightness = max(sunLight, MIN_SUN_BRIGHTNESS)
        val visible: Rectangle = gameContextRepository.getCameraContext().visibleWorld
        val padding = 1
        val minX = floor(visible.x).toInt() - padding
        val minY = max(0, floor(visible.y).toInt() - padding)
        val maxX = ceil(visible.x + visible.width).toInt() + padding
        val maxY = floor(visible.y + visible.height).toInt() + padding
        val clampedMaxY = maxY.coerceAtMost(gameWorld.height - 1)

        val w = maxX - minX + 1
        val h = clampedMaxY - minY + 1
        if (w <= 0 || h <= 0) return

        val pix = ensurePixmap(w, h)
        val raw = ensureRawBuffer(w * h)

        for (j in 0 until h) {
            val worldY = minY + j
            for (i in 0 until w) {
                raw[i + j * w] = computeCellBrightness(minX + i, worldY, sunBrightness)
            }
        }

        for (j in 0 until h) {
            for (i in 0 until w) {
                val center = raw[i + j * w]
                val left = if (i > 0) raw[(i - 1) + j * w] else center
                val right = if (i < w - 1) raw[(i + 1) + j * w] else center
                val up = if (j > 0) raw[i + (j - 1) * w] else center
                val down = if (j < h - 1) raw[i + (j + 1) * w] else center
                val avg = (center + left + right + up + down) * 0.2f
                val brightness = max(center, avg)
                val channel = (brightness * 255f).toInt().coerceIn(0, 255)
                val rgba = (channel shl 24) or (channel shl 16) or (channel shl 8) or 0xFF
                pix.drawPixel(i, j, rgba)
            }
        }

        val tex = ensureTexture(pix)
        tex.draw(pix, 0, 0)
        val flippedRegion = ensureRegion(tex)

        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO)
        spriteBatch.begin()
        spriteBatch.color = Color.WHITE
        spriteBatch.draw(
            /* region = */ flippedRegion,
            /* x = */ minX.toFloat(),
            /* y = */ minY.toFloat(),
            /* width = */ w.toFloat(),
            /* height = */ h.toFloat(),
        )
        spriteBatch.end()
        spriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun computeCellBrightness(worldX: Int, worldY: Int, sunBrightness: Float): Float {
        if (worldY < 0 || worldY >= gameWorld.height) return 1f

        val selfOpaque = grid.isOpaqueAt(worldX, worldY)
        val selfEffective = grid.effective(worldX, worldY, sunBrightness)

        var effective = selfEffective
        var hasNonOpaqueNeighbour = !selfOpaque

        for ((dx, dy) in NEIGHBOURS) {
            val nx = worldX + dx
            val ny = worldY + dy
            if (ny < 0 || ny >= gameWorld.height) continue
            val neighbourOpaque = grid.isOpaqueAt(nx, ny)
            if (neighbourOpaque) continue
            hasNonOpaqueNeighbour = true
            if (selfOpaque) {
                val neighbourEffective = grid.effective(nx, ny, sunBrightness)
                if (neighbourEffective > effective) {
                    effective = neighbourEffective
                }
            }
        }

        val withFloor = if (hasNonOpaqueNeighbour) max(effective, MIN_AMBIENT) else effective
        return withFloor.coerceIn(0f, 1f)
    }

    private fun ensurePixmap(width: Int, height: Int): Pixmap {
        val current = pixmap
        if (current != null && pixmapWidth == width && pixmapHeight == height) {
            return current
        }
        current?.dispose()
        texture?.dispose()
        val newPix = Pixmap(width, height, Pixmap.Format.RGBA8888)
        newPix.blending = Pixmap.Blending.None
        pixmap = newPix
        pixmapWidth = width
        pixmapHeight = height
        texture = null
        region = null
        return newPix
    }

    private fun ensureTexture(pix: Pixmap): Texture {
        texture?.let { return it }
        val tex = Texture(pix.width, pix.height, Pixmap.Format.RGBA8888).apply {
            setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge)
        }
        texture = tex
        region = null
        return tex
    }

    private fun ensureRegion(tex: Texture): TextureRegion {
        region?.let { return it }
        val newRegion = TextureRegion(tex).apply { flip(false, true) }
        region = newRegion
        return newRegion
    }

    private fun ensureRawBuffer(size: Int): FloatArray {
        if (size <= rawCapacity) return rawBrightness
        val newBuffer = FloatArray(size)
        rawBrightness = newBuffer
        rawCapacity = size
        return newBuffer
    }

    override fun dispose() {
        spriteBatch.dispose()
        pixmap?.dispose()
        texture?.dispose()
        pixmap = null
        texture = null
        region = null
    }

    companion object {
        private const val MIN_SUN_BRIGHTNESS = 0.2f
        private const val MIN_AMBIENT = 1f / LightGrid.MAX_LEVEL_F

        private val NEIGHBOURS = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(-1, 0),
            intArrayOf(0, 1),
            intArrayOf(0, -1),
        )
    }
}
