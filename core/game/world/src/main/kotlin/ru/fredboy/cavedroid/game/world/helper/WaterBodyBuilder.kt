package ru.fredboy.cavedroid.game.world.helper

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import ru.fredboy.cavedroid.domain.world.model.ContactSensorType

class WaterBodyBuilder(
    private val world: World,
) {
    private var waterBody: Body? = null

    fun buildWaterBodies(waterTiles: Set<Pair<Int, Int>>) {
        destroyPrevious()

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
        }

        val body = world.createBody(bodyDef)

        // Merge and generate rectangular water zones
        val mergedRects = mergeTilesIntoRects(waterTiles)

        mergedRects.forEach { rect ->
            val centerX = rect.x + rect.width / 2f
            val centerY = rect.y + rect.height / 2f

            val shape = PolygonShape().apply {
                setAsBox(rect.width / 2f, rect.height / 2f, Vector2(centerX, centerY), 0f)
            }

            val fixtureDef = FixtureDef().apply {
                this.shape = shape
                isSensor = true // detect player in water, but not block them
                density = 1f
            }

            body.createFixture(fixtureDef).apply {
                userData = ContactSensorType.BLOCK_WATER
            }
            shape.dispose()
        }

        this.waterBody = body
    }

    private fun destroyPrevious() {
        waterBody?.let { body ->
            world.destroyBody(body)
        }
        waterBody = null
    }

    private data class Rect(val x: Int, val y: Int, val width: Int, val height: Int)

    private fun mergeTilesIntoRects(tiles: Set<Pair<Int, Int>>): List<Rect> {
        val remaining = tiles.toMutableSet()
        val result = mutableListOf<Rect>()

        while (remaining.isNotEmpty()) {
            val (startX, startY) = remaining.first()
            var width = 1
            var height = 1

            // Try to grow width as far as possible on the same row
            while ((startX + width to startY) in remaining) {
                width++
            }

            // Check if we can extend this rectangle vertically
            var maxHeight = 1
            outer@ while (true) {
                for (dx in 0 until width) {
                    if ((startX + dx to startY + maxHeight) !in remaining) break@outer
                }
                maxHeight++
            }

            // Remove all tiles used in the rectangle
            for (dx in 0 until width) {
                for (dy in 0 until maxHeight) {
                    remaining.remove(startX + dx to startY + dy)
                }
            }

            result.add(Rect(startX, startY, width, maxHeight))
        }

        return result
    }
}
