package ru.fredboy.cavedroid.game.world

import box2dLight.DirectionalLight
import box2dLight.Light
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Filter
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.PhysicsConstants
import ru.fredboy.cavedroid.entity.mob.model.Mob
import javax.inject.Inject
import kotlin.math.max

@GameScope
class GameWorldLightManager @Inject constructor(
    private val gameContextRepository: GameContextRepository,
) : OnBlockPlacedListener,
    OnBlockDestroyedListener,
    Disposable {

    private var _gameWorld: GameWorld? = null

    private var _rayHandler: RayHandler? = null

    private var _sunLight: DirectionalLight? = null

    private val gameWorld: GameWorld
        get() = requireNotNull(_gameWorld)

    private val sunLight: DirectionalLight
        get() = requireNotNull(_sunLight)

    val rayHandler: RayHandler
        get() = requireNotNull(_rayHandler)

    private val blockLights = mutableMapOf<Triple<Int, Int, Layer>, Light>()

    fun attachToGameWorld(gameWorld: GameWorld) {
        if (_gameWorld != null) {
            Gdx.app.error(TAG, "GameWorldLightManager already attached")
            return
        }

        _gameWorld = gameWorld

        _rayHandler = RayHandler(gameWorld.world)

        _sunLight = DirectionalLight(rayHandler, 512, Color().apply { a = 1f }, 90.1f).apply {
            val filter = Filter().apply {
                maskBits = PhysicsConstants.CATEGORY_OPAQUE
            }
            setContactFilter(filter)
            setSoftnessLength(5f)
        }

        for (x in 0 until gameWorld.width) {
            for (y in 0 until gameWorld.height) {
                onBlockPlaced(gameWorld.getForeMap(x, y), x, y, Layer.FOREGROUND)
                onBlockPlaced(gameWorld.getBackMap(x, y), x, y, Layer.BACKGROUND)
            }
        }

        gameWorld.addBlockPlacedListener(this)
        gameWorld.addBlockDestroyedListener(this)
    }

    override fun onBlockPlaced(block: Block, x: Int, y: Int, layer: Layer) {
        val lightInfo = block.params.lightInfo ?: return

        blockLights[Triple(x, y, layer)] = PointLight(
            rayHandler,
            128,
            Color().apply { a = lightInfo.lightBrightness },
            lightInfo.lightDistance,
            x + 0.5f,
            y + 0.5f,
        ).apply {
            val filter = Filter().apply {
                maskBits = PhysicsConstants.CATEGORY_OPAQUE
            }
            setContactFilter(filter)
            setSoftnessLength(3f)
        }
    }

    override fun onBlockDestroyed(
        block: Block,
        x: Int,
        y: Int,
        layer: Layer,
        withDrop: Boolean,
        destroyedByPlayer: Boolean,
    ) {
        val light = blockLights.remove(Triple(x, y, layer))
        light?.remove(true)
    }

    override fun dispose() {
        gameWorld.removeBlockPlacedListener(this)
        gameWorld.removeBlockDestroyedListener(this)
        _gameWorld = null

        rayHandler.dispose()
        _rayHandler = null
        _sunLight = null
        blockLights.clear()
    }

    fun isMobExposedToSun(mob: Mob): Boolean {
        return sunLight.contains(mob.position.x, mob.position.y)
    }

    fun update() {
        var sunAngle = (gameWorld.getNormalizedTime() * 30f + 75f)
        if (sunAngle >= 120f) sunAngle -= 60f
        if (MathUtils.isEqual(90f, sunAngle, 0.1f)) {
            sunAngle = 90.1f
        }
        sunLight.direction = sunAngle
        sunLight.color = Color().apply { a = max(gameWorld.getSunlight(), 0.1f) }

        val visibleWorld = gameContextRepository.getCameraContext().visibleWorld
        blockLights.forEach { (_, light) ->
            light.isActive = visibleWorld.overlaps(
                Rectangle(
                    light.x - light.distance,
                    light.y - light.distance,
                    light.distance * 2,
                    light.distance * 2,
                ),
            )
        }
    }

    companion object {
        private const val TAG = "GameWorldLightManager"
    }
}
