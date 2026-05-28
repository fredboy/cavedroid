package ru.fredboy.cavedroid.game.world

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.coroutines.GdxMainThread
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.removeFirst
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Biome
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.game.world.generator.GameWorldGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import ru.fredboy.cavedroid.game.world.lighting.LightingSystem
import java.lang.ref.WeakReference
import java.util.LinkedList
import javax.inject.Inject
import kotlin.random.Random

@GameScope
class GameWorld @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val physicsController: GameWorldContactListener,
    private val gameWorldSolidBlockBodiesManager: GameWorldSolidBlockBodiesManager,
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
    val lightingSystem: LightingSystem,
    initialForeMap: Array<Array<Block>>?,
    initialBackMap: Array<Array<Block>>?,
    initialBiomes: Array<Biome>?,
    requestedWidth: Int?,
) : Disposable {
    val foreMap: Array<Array<Block>>
    val backMap: Array<Array<Block>>
    val biomes: Array<Biome>

    val width: Int
    val height: Int

    var currentGameTime = DAY_DURATION_SEC * 0.125f
    var totalGameTimeSec = currentGameTime
    var lastSpawnGameTime = 0f

    var currentStreakStartDayIndex = 0

    var moonPhase = 0

    var weather: Weather = Weather.CLEAR
    var weatherTimer: Float = nextWeatherDuration(Weather.CLEAR)
    var weatherIntensity: Float = 0f

    val generatorConfig: WorldGeneratorConfig = WorldGeneratorConfig.getDefault(
        width = initialForeMap?.size ?: requestedWidth ?: WorldGeneratorConfig.DEFAULT_WIDTH,
    )

    val world: World = World(Vector2(0f, 32f), false)

    private var timeMultiplier = 1f

    private var skipNight = false
        set(value) {
            timeMultiplier = if (value) {
                200f
            } else {
                1f
            }
            field = value
        }

    private var box2dAccumulator: Float = 0f

    private val onBlockPlacedListeners = LinkedList<WeakReference<OnBlockPlacedListener>>()
    private val onBlockDestroyedListeners = LinkedList<WeakReference<OnBlockDestroyedListener>>()

    init {
        width = generatorConfig.width
        height = generatorConfig.height

        if (initialForeMap != null && initialBackMap != null) {
            foreMap = initialForeMap
            backMap = initialBackMap
            biomes = initialBiomes ?: Array(width) { Biome.PLAINS }
        } else {
            val generated = GameWorldGenerator(generatorConfig, itemsRepository).generate()
            foreMap = generated.foreMap
            backMap = generated.backMap
            biomes = generated.biomes
        }

        physicsController.attachToGameWorld(this)
        gameWorldSolidBlockBodiesManager.attachToGameWorld(this)
        lightingSystem.attachToGameWorld(this)
    }

    fun addBlockPlacedListener(listener: OnBlockPlacedListener) {
        onBlockPlacedListeners.add(WeakReference(listener))
    }

    fun addBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        onBlockDestroyedListeners.add(WeakReference(listener))
    }

    fun removeBlockPlacedListener(listener: OnBlockPlacedListener) {
        onBlockPlacedListeners.removeFirst { it.get() == listener }
    }

    fun removeBlockDestroyedListener(listener: OnBlockDestroyedListener) {
        onBlockDestroyedListeners.removeFirst { it.get() == listener }
    }

    private fun transformX(x: Int): Int {
        var transformed = x % width
        if (transformed < 0) {
            transformed = width + x
        }
        return transformed
    }

    fun getBiomeAt(x: Int): Biome = biomes[transformX(x)]

    fun biomeProximityFactor(
        centerX: Float,
        rangeBlocks: Float,
        predicate: (Biome) -> Boolean,
    ): Float {
        val centerBlock = MathUtils.floor(centerX)
        if (predicate(getBiomeAt(centerBlock))) return 1f

        val maxSteps = MathUtils.ceil(rangeBlocks) + 1
        var minDistance = Float.POSITIVE_INFINITY

        for (d in 1..maxSteps) {
            if (predicate(getBiomeAt(centerBlock - d))) {
                // matching block's right edge is at (centerBlock - d + 1)
                minDistance = centerX - (centerBlock - d + 1).toFloat()
                break
            }
        }
        for (d in 1..maxSteps) {
            if (predicate(getBiomeAt(centerBlock + d))) {
                // matching block's left edge is at (centerBlock + d)
                val dist = (centerBlock + d).toFloat() - centerX
                if (dist < minDistance) minDistance = dist
                break
            }
        }

        if (minDistance >= rangeBlocks) return 0f
        return 1f - minDistance / rangeBlocks
    }

    private fun nextWeatherDuration(next: Weather): Float = when (next) {
        Weather.CLEAR -> Random.nextFloat() * (CLEAR_MAX_SEC - CLEAR_MIN_SEC) + CLEAR_MIN_SEC
        Weather.RAIN -> Random.nextFloat() * (RAIN_MAX_SEC - RAIN_MIN_SEC) + RAIN_MIN_SEC
    }

    private fun getMap(x: Int, y: Int, layer: Layer): Block {
        val fallback = itemsRepository.fallbackBlock

        if (y !in 0..<height) {
            return fallback
        }

        val transformedX = transformX(x)

        if (transformedX !in 0..<width) {
            return fallback
        }

        return when (layer) {
            Layer.FOREGROUND -> foreMap[transformedX][y]
            Layer.BACKGROUND -> backMap[transformedX][y]
        }
    }

    private fun notifyBlockPlaced(x: Int, y: Int, layer: Layer, value: Block) {
        onBlockPlacedListeners.removeAll { listener ->
            listener.get()?.let {
                it.onBlockPlaced(value, x, y, layer)
                return@removeAll false
            }

            logger.w("An empty OnBlockPlacedListener weak reference was removed!")
            true
        }
    }

    private fun notifyBlockDestroyed(
        x: Int,
        y: Int,
        layer: Layer,
        value: Block,
        withDrop: Boolean,
        destroyedByPlayer: Boolean,
    ) {
        onBlockDestroyedListeners.removeAll { listener ->
            listener.get()?.let {
                it.onBlockDestroyed(value, x, y, layer, withDrop, destroyedByPlayer)
                return@removeAll false
            }

            logger.w { "An empty OnBlockDestroyedListener weak reference was removed!" }
            true
        }
    }

    private fun setMap(x: Int, y: Int, layer: Layer, value: Block, dropOld: Boolean, destroyedByPlayer: Boolean) {
        if (!GdxMainThread.isMainThread()) {
            logger.w {
                "setMap($x, $y, $layer) called off the main thread from ${Thread.currentThread().name}"
            }
        }

        if (y !in 0..<height) {
            return
        }

        val transformedX = transformX(x)

        if (transformedX !in 0..<width) {
            return
        }

        val currentBlock = getMap(transformedX, y, layer)

        if (currentBlock == value) {
            return
        }

        currentBlock
            .takeIf { !it.isNone() }
            ?.let { currentBlock ->
                notifyBlockDestroyed(transformedX, y, layer, currentBlock, dropOld, destroyedByPlayer)
            }

        when (layer) {
            Layer.FOREGROUND -> foreMap[transformedX][y] = value
            Layer.BACKGROUND -> backMap[transformedX][y] = value
        }

        notifyBlockPlaced(transformedX, y, layer, value)
    }

    private fun isSameSlab(slab1: Block, slab2: Block): Boolean {
        return slab1 is Block.Slab &&
            slab2 is Block.Slab &&
            (slab1.params.key == slab2.otherPartBlockKey || slab1.otherPartBlockKey == slab2.params.key)
    }

    fun hasForeAt(x: Int, y: Int): Boolean = !getMap(x, y, Layer.FOREGROUND).isNone()

    fun hasBackAt(x: Int, y: Int): Boolean = !getMap(x, y, Layer.BACKGROUND).isNone()

    fun getForeMap(x: Int, y: Int): Block = getMap(x, y, Layer.FOREGROUND)

    fun setForeMap(x: Int, y: Int, block: Block, dropOld: Boolean = false, destroyedByPlayer: Boolean = false) {
        setMap(x, y, Layer.FOREGROUND, block, dropOld, destroyedByPlayer)
    }

    fun resetForeMap(x: Int, y: Int) {
        setForeMap(x, y, itemsRepository.fallbackBlock)
    }

    fun getBackMap(x: Int, y: Int): Block = getMap(x, y, Layer.BACKGROUND)

    fun setBackMap(x: Int, y: Int, block: Block, dropOld: Boolean = false, destroyedByPlayer: Boolean = false) {
        setMap(x, y, Layer.BACKGROUND, block, dropOld, destroyedByPlayer)
    }

    fun canPlaceToForeground(x: Int, y: Int, value: Block): Boolean {
        return !hasForeAt(x, y) || value.isNone() || getForeMap(x, y).params.replaceable
    }

    fun placeToForeground(
        x: Int,
        y: Int,
        value: Block,
        dropOld: Boolean = false,
        destroyedByPlayer: Boolean = false,
    ): Boolean {
        val wasPlaced = if (canPlaceToForeground(x, y, value)) {
            setForeMap(x, y, value, dropOld, destroyedByPlayer)
            true
        } else if (value is Block.Slab && isSameSlab(value, getForeMap(x, y))) {
            setForeMap(x, y, itemsRepository.getBlockByKey(value.fullBlockKey), dropOld, destroyedByPlayer)
            true
        } else {
            false
        }

        return wasPlaced
    }

    fun placeToBackground(
        x: Int,
        y: Int,
        value: Block,
        dropOld: Boolean = false,
        destroyedByPlayer: Boolean = false,
    ): Boolean {
        val wasPlaced = if (value.isNone() ||
            getBackMap(x, y).isNone() &&
            value.params.hasCollision &&
            (!value.params.isTransparent || value.params.key == "glass" || value.isChest() || value.isSlab())
        ) {
            setBackMap(x, y, value, dropOld, destroyedByPlayer)
            true
        } else {
            false
        }

        return wasPlaced
    }

    fun destroyForeMap(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean = false) {
        placeToForeground(x, y, itemsRepository.fallbackBlock, shouldDrop, destroyedByPlayer)
    }

    fun destroyBackMap(x: Int, y: Int, shouldDrop: Boolean, destroyedByPlayer: Boolean = false) {
        placeToBackground(x, y, itemsRepository.fallbackBlock, shouldDrop, destroyedByPlayer)
    }

    /**
     * Returns the value between 0 and 1 representing the daylight
     */
    fun getSunlight(): Float {
        return (MathUtils.sin(currentGameTime / DAY_DURATION_SEC * MathUtils.PI2) + 1f) / 2f
    }

    fun getNormalizedTime(): Float {
        return currentGameTime / DAY_DURATION_SEC * 2
    }

    fun isDayTime(): Boolean {
        return currentGameTime < DAY_DURATION_SEC / 2f
    }

    fun skipNight() {
        if (isDayTime()) {
            return
        }

        skipNight = true
    }

    fun getLightAt(x: Int, y: Int): Float {
        return lightingSystem.getEffectiveBrightness(x, y, getSunlight())
    }

    fun update(delta: Float) {
        currentGameTime += (delta * timeMultiplier)
        totalGameTimeSec += (delta * timeMultiplier)

        if (currentGameTime >= DAY_DURATION_SEC) {
            currentGameTime -= DAY_DURATION_SEC
            moonPhase = (moonPhase + 1) % environmentTextureRegionsRepository.getMoonPhasesCount()
        }

        if (isDayTime()) {
            skipNight = false
        }

        weatherTimer -= delta * timeMultiplier
        if (weatherTimer <= 0f) {
            weather = if (weather == Weather.CLEAR) Weather.RAIN else Weather.CLEAR
            weatherTimer = nextWeatherDuration(weather)
        }

        val intensityTarget = if (weather == Weather.RAIN) 1f else 0f
        val intensityStep = delta / WEATHER_FADE_SEC
        weatherIntensity = if (weatherIntensity < intensityTarget) {
            (weatherIntensity + intensityStep).coerceAtMost(intensityTarget)
        } else {
            (weatherIntensity - intensityStep).coerceAtLeast(intensityTarget)
        }

        lightingSystem.update(delta)

        box2dAccumulator += delta
        while (box2dAccumulator >= PHYSICS_STEP_DELTA) {
            world.step(PHYSICS_STEP_DELTA, 6, 2)
            box2dAccumulator -= PHYSICS_STEP_DELTA
        }
    }

    override fun dispose() {
        lightingSystem.dispose()
        physicsController.dispose()
        gameWorldSolidBlockBodiesManager.dispose()
        world.dispose()
    }

    companion object {
        private const val TAG = "GameWorld"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)

        private const val PHYSICS_STEP_DELTA = 1f / 60f

        const val DAY_DURATION_SEC = 1200

        private const val CLEAR_MIN_SEC = 5f * 60f
        private const val CLEAR_MAX_SEC = 15f * 60f
        private const val RAIN_MIN_SEC = 1f * 60f
        private const val RAIN_MAX_SEC = 5f * 60f

        private const val WEATHER_FADE_SEC = 15f
    }
}
