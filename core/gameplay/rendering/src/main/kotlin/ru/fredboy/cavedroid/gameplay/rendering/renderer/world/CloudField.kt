package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.utils.meters
import kotlin.random.Random

internal class CloudField(seed: Long, private val seaLevel: Int) {

    private var elapsedSeconds = 0.0

    // Keep the layout stable between frames and sessions, independently of the stars.
    val clouds: List<Cloud> = Random(seed xor 0x434C4F5544L).let { random ->
        List(COLUMNS * ROWS) { index ->
            Cloud(
                // Each altitude band covers the entire repeat, without large empty stretches.
                x = (index % COLUMNS + random.nextFloat() * 0.5f) * COLUMN_WIDTH,
                // Reach the screen's top at camera Y=64 with a 24-block viewport,
                // keeping the lower edge at 6.5 blocks below the landscape's top.
                y = CREST_OFFSET + (index / COLUMNS + random.nextFloat()) * ROW_HEIGHT,
                width = random.nextInt(16, 49).meters,
                height = random.nextInt(3, 9).meters,
            )
        }
    }

    fun update(delta: Float) {
        elapsedSeconds += delta.toDouble()
    }

    fun forEachVisibleCloud(viewport: Rectangle, drawCloud: (Cloud, Float, Float) -> Unit) {
        // Anchor the band to the landscape's top edge, with slower vertical scrolling.
        val verticalOffset = viewport.y +
            parallaxScreenY(viewport.y, viewport.height, seaLevel.toFloat()) * VERTICAL_PARALLAX_FACTOR

        for (cloud in clouds) {
            val y = cloud.y + verticalOffset
            if (y + cloud.height <= viewport.y || y >= viewport.y + viewport.height) continue

            // Smaller clouds appear farther away, so both parallax and wind move them more slowly.
            val depthFactor = cloud.width / MAX_CLOUD_WIDTH
            val offset = viewport.x.toDouble() * SCROLL_FACTOR * depthFactor -
                elapsedSeconds * WIND_SPEED * depthFactor
            var scrollOffset = (offset % HORIZONTAL_SPAN).toFloat()
            if (scrollOffset < 0f) scrollOffset += HORIZONTAL_SPAN
            var x = viewport.x - scrollOffset + cloud.x
            // Include the previous repeat so clouds cross the left edge without popping.
            if (x > viewport.x) x -= HORIZONTAL_SPAN
            while (x < viewport.x + viewport.width) {
                if (x + cloud.width > viewport.x) drawCloud(cloud, x, y)
                x += HORIZONTAL_SPAN
            }
        }
    }

    data class Cloud(val x: Float, val y: Float, val width: Float, val height: Float)

    companion object {
        private const val COLUMNS = 8
        private const val ROWS = 8
        private const val HORIZONTAL_SPAN = 64f
        private const val COLUMN_WIDTH = HORIZONTAL_SPAN / COLUMNS
        private const val CREST_OFFSET = -9.5f
        private const val ROW_HEIGHT = 2f

        // The landscape scrolls at 0.5 of camera speed; clouds move slightly slower.
        private const val SCROLL_FACTOR = 0.2f
        private const val MAX_CLOUD_WIDTH = 3f

        // Blocks per second for the nearest clouds; distant clouds share the same wind direction.
        private const val WIND_SPEED = 0.1f
        private const val VERTICAL_PARALLAX_FACTOR = 0.8f
    }
}
