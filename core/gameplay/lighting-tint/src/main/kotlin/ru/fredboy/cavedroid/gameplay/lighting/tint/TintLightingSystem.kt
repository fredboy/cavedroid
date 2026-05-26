package ru.fredboy.cavedroid.gameplay.lighting.tint

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import ru.fredboy.cavedroid.domain.world.lighting.LightHandle
import ru.fredboy.cavedroid.entity.mob.model.Mob
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem

class TintLightingSystem : LightingSystem {

    private var _gameWorld: GameWorld? = null

    private val gameWorld: GameWorld
        get() = requireNotNull(_gameWorld)

    private val shaper = ShapeRenderer()

    override val chunkSize: Int = 1

    override fun attachToGameWorld(gameWorld: GameWorld) {
        _gameWorld = gameWorld
    }

    override fun refreshChunks(chunks: Iterable<Pair<Int, Int>>) = Unit

    override fun update(delta: Float) = Unit

    override fun recalculate() = Unit

    override fun render(camera: OrthographicCamera, cameraJumped: Boolean) {
        val darkness = (1f - gameWorld.getSunlight()).coerceIn(0f, 1f) * MAX_NIGHT_ALPHA
        if (darkness <= 0f) return

        shaper.projectionMatrix = camera.combined

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shaper.begin(ShapeRenderer.ShapeType.Filled)
        shaper.color = Color(NIGHT_COLOR.r, NIGHT_COLOR.g, NIGHT_COLOR.b, darkness)
        shaper.rect(
            /* x = */ camera.position.x - camera.viewportWidth / 2f,
            /* y = */ camera.position.y - camera.viewportHeight / 2f,
            /* width = */ camera.viewportWidth,
            /* height = */ camera.viewportHeight,
        )
        shaper.end()

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun isMobExposedToSun(mob: Mob): Boolean = gameWorld.isDayTime()

    override fun createPlayerSightLight(body: Body, x: Float, y: Float): LightHandle = NoOpLightHandle

    override fun createFurnaceLight(x: Float, y: Float): LightHandle = NoOpLightHandle

    override fun createFireLight(x: Float, y: Float): LightHandle = NoOpLightHandle

    override fun dispose() {
        shaper.dispose()
        _gameWorld = null
    }

    private object NoOpLightHandle : LightHandle {
        override var isActive: Boolean = true
        override fun setPosition(x: Float, y: Float) = Unit
        override fun update() = Unit
        override fun dispose() = Unit
    }

    companion object {
        private val NIGHT_COLOR = Color(0f, 0f, 0.08f, 1f)
        private const val MAX_NIGHT_ALPHA = 0.85f
    }
}
