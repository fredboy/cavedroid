package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.math.Rectangle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CloudFieldTest {

    private fun visible(field: CloudField, viewport: Rectangle): List<Pair<CloudField.Cloud, Rectangle>> = buildList {
        field.forEachVisibleCloud(viewport) { cloud, x, y ->
            add(cloud to Rectangle(x, y, cloud.width, cloud.height))
        }
    }

    @Test
    fun `cloud field stays visible throughout long horizontal travel and wind drift`() {
        for (seed in 0L..15L) {
            val field = CloudField(seed, 128)
            for (cameraX in -8192..8192 step 17) {
                field.update(60f)
                val viewport = Rectangle(cameraX.toFloat(), 64f, 16f, 64f)
                val clouds = visible(field, viewport)
                assertFalse(clouds.isEmpty(), "seed=$seed, x=$cameraX")
                assertTrue(clouds.all { (_, bounds) -> bounds.overlaps(viewport) })
            }
        }
    }

    @Test
    fun `smaller clouds scroll slower when the camera moves`() {
        val field = CloudField(42L, 128)
        val before = visible(field, Rectangle(0f, 64f, 64f, 64f)).toMap()
        val after = visible(field, Rectangle(1f, 64f, 64f, 64f)).toMap()
        val shifts = field.clouds.sortedBy { it.width }.map { cloud ->
            val screenShift = wrappedShift(before.getValue(cloud).x, after.getValue(cloud).x - 1f)
            assertTrue(screenShift < 0f && screenShift >= -0.201f)
            screenShift
        }
        assertTrue(shifts.first() > shifts.last())
        assertTrue(shifts.zipWithNext().all { (smaller, larger) -> smaller >= larger - 0.0001f })
    }

    @Test
    fun `wind moves clouds with a stationary camera and is independent of frame rate`() {
        val field = CloudField(42L, 128)
        val steppedField = CloudField(42L, 128)
        val viewport = Rectangle(0f, 64f, 64f, 64f)
        val before = visible(field, viewport).toMap()
        field.update(1f)
        repeat(60) { steppedField.update(1f / 60f) }
        val after = visible(field, viewport).toMap()
        val stepped = visible(steppedField, viewport).toMap()
        val shifts = field.clouds.sortedBy { it.width }.map { cloud ->
            assertEquals(after.getValue(cloud).x, stepped.getValue(cloud).x, 0.0001f)
            val shift = wrappedShift(before.getValue(cloud).x, after.getValue(cloud).x)
            assertTrue(shift > 0f && shift <= 0.101f)
            shift
        }
        assertTrue(shifts.first() < shifts.last())
        assertTrue(shifts.zipWithNext().all { (smaller, larger) -> smaller <= larger + 0.0001f })
    }

    private fun wrappedShift(before: Float, after: Float): Float = (after - before + 96f) % 64f - 32f

    @Test
    fun `clouds scroll vertically slower than the landscape across zoom levels and the sea level clamp`() {
        val field = CloudField(42L, 128)
        for ((cameraY, height) in listOf(32f to 24f, 64f to 32f, 96f to 64f, 127.9f to 24f, 128.1f to 24f)) {
            val before = visible(field, Rectangle(0f, cameraY, 40f, height)).toMap()
            assertFalse(before.isEmpty())
            for (movement in listOf(-0.5f, 0.5f)) {
                val after = visible(field, Rectangle(0f, cameraY + movement, 40f, height)).toMap()
                val shared = before.keys.intersect(after.keys)
                assertFalse(shared.isEmpty())
                val landscapeDelta = parallaxScreenY(cameraY + movement, height, 128f) -
                    parallaxScreenY(cameraY, height, 128f)
                for (cloud in shared) {
                    val screenDelta = after.getValue(cloud).y - before.getValue(cloud).y - movement
                    assertEquals(landscapeDelta * 0.8f, screenDelta, 0.001f)
                    assertEquals(before.getValue(cloud).x, after.getValue(cloud).x)
                }
            }
        }
    }

    @Test
    fun `cloud band stays visible around hill crests at normal camera heights and zoom levels`() {
        val field = CloudField(42L, 128)
        for (height in listOf(24f, 32f, 64f)) {
            for (cameraY in listOf(32f, 64f, 96f, 112f, 128f)) {
                val viewport = Rectangle(0f, cameraY, 40f, height)
                val clouds = visible(field, viewport)
                assertFalse(clouds.isEmpty(), "height=$height, cameraY=$cameraY")
                val hillTop = parallaxScreenY(cameraY, height, 128f)
                for ((_, bounds) in clouds) {
                    val offsetFromHillTop = bounds.y - cameraY - hillTop
                    assertTrue(offsetFromHillTop in -20f..6.5f)
                }
            }
        }
    }

    @Test
    fun `expanded band reaches the screen top at camera y64 and preserves the lower edge`() {
        for (seed in 0L..15L) {
            val field = CloudField(seed, 128)
            val viewport = Rectangle(160f, 64f, 40f, 24f)
            val clouds = visible(field, viewport)
            assertFalse(clouds.isEmpty())
            assertTrue(clouds.minOf { (_, bounds) -> bounds.y - viewport.y } in 0f..2.1f)
            assertTrue(field.clouds.maxOf { it.y } in 4.5f..6.5f)
            // The area doubled from 64x8 to 64x16, retaining the original density.
            assertEquals(64, field.clouds.size)
        }
    }

    @Test
    fun `seed reproduces the layout and clouds remain small and near the texture crests`() {
        val field = CloudField(42L, 128)
        assertEquals(field.clouds, CloudField(42L, 128).clouds)
        assertNotEquals(field.clouds, CloudField(43L, 128).clouds)
        for (cloud in field.clouds) {
            assertTrue(cloud.y >= -9.5f && cloud.y < 6.5f)
            assertTrue(cloud.width in 1f..3f)
            assertTrue(cloud.height in 0.1875f..0.5f)
        }
    }
}
