package ru.fredboy.cavedroid.ux.physics.impl

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import javax.inject.Inject

@GameScope
class GameWorldSolidBlockBodiesManagerImpl @Inject constructor() : GameWorldSolidBlockBodiesManager() {

    override fun initialize() {
        for (x in 0..<gameWorld.width) {
            for (y in 0..<gameWorld.height) {
                gameWorld.getForeMap(x, y).createBody(x, y)?.let { body ->
                    _bodies[x to y] = body
                }
            }
        }
    }

    override fun onBlockPlaced(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
    ) {
        if (layer != Layer.FOREGROUND) {
            return
        }

        updateBodiesAround(x, y)
    }

    private fun updateBodiesAround(centerX: Int, centerY: Int) {
        for (x in centerX - 1..centerX + 1) {
            for (y in centerY - 1..centerY + 1) {
                gameWorld.getForeMap(x, y).updateBody(x, y)
            }
        }
    }

    private fun Block.createBody(x: Int, y: Int): Body? {
        if (!isSolidSurfaceBlock(x, y)) {
            return null
        }

        val rect = getRectangle(x, y)

        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            rect.getCenter(position)
            fixedRotation = true
        }

        val shape = PolygonShape().apply {
            setAsBox(rect.width / 2f, rect.height / 2f)
        }

        val fixtureDef = FixtureDef().apply {
            this.shape = shape
            density = 1f
            friction = .2f
            restitution = 0f
            filter.categoryBits = PhysicsConstants.CATEGORY_BLOCK
        }

        return world.createBody(bodyDef).also { body ->
            body.createFixture(fixtureDef)
            body.userData = this
            shape.dispose()
        }
    }

    private fun Block.updateBody(x: Int, y: Int): Body? {
        val body = _bodies.remove(x to y)?.takeIf {
            if (it.userData == this && isSolidSurfaceBlock(x, y)) {
                true
            } else {
                world.destroyBody(it)
                false
            }
        } ?: this.createBody(x, y)

        return body?.also { _bodies[x to y] = body }
    }

    private fun isSolidSurfaceBlock(x: Int, y: Int): Boolean {
        val block = gameWorld.getForeMap(x, y)

        return block.params.hasCollision &&
            (
                !gameWorld.getForeMap(x - 1, y).params.hasCollision ||
                    !gameWorld.getForeMap(x + 1, y).params.hasCollision ||
                    !gameWorld.getForeMap(x, y - 1).params.hasCollision ||
                    !gameWorld.getForeMap(x, y + 1).params.hasCollision
                )
    }
}
