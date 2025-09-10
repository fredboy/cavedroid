package ru.fredboy.cavedroid.game.world

import box2dLight.RayHandler
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.removeFirst
import ru.fredboy.cavedroid.domain.assets.repository.EnvironmentTextureRegionsRepositoryTexture
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.domain.world.listener.OnBlockDestroyedListener
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.game.world.abstraction.GameWorldSolidBlockBodiesManager
import ru.fredboy.cavedroid.game.world.generator.GameWorldGenerator
import ru.fredboy.cavedroid.game.world.generator.WorldGeneratorConfig
import java.lang.ref.WeakReference
import java.util.*
import javax.inject.Inject

@GameScope
class GameWorld @Inject constructor(
    private val itemsRepository: ItemsRepository,
    private val physicsController: GameWorldContactListener,
    private val gameWorldSolidBlockBodiesManager: GameWorldSolidBlockBodiesManager,
    private val environmentTextureRegionsRepository: EnvironmentTextureRegionsRepositoryTexture,
    private val gameWorldLightManager: GameWorldLightManager,
    initialForeMap: Array<Array<Block>>?,
    initialBackMap: Array<Array<Block>>?,
) : Disposable {
    val foreMap: Array<Array<Block>>
    val backMap: Array<Array<Block>>

    val width: Int
    val height: Int

    var currentGameTime = DAY_DURATION_SEC * 0.125f
    var totalGameTimeSec = currentGameTime
    var lastSpawnGameTime = 0f

    var moonPhase = 0

    val generatorConfig = WorldGeneratorConfig.getDefault()

    val world: World = World(Vector2(0f, 9.8f), false)

    val rayHandler: RayHandler
        get() = gameWorldLightManager.rayHandler

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
        } else {
            val (generatedFore, generatedBack) = GameWorldGenerator(generatorConfig, itemsRepository).generate()
            foreMap = generatedFore
            backMap = generatedBack
        }

        physicsController.attachToGameWorld(this)
        gameWorldSolidBlockBodiesManager.attachToGameWorld(this)
        gameWorldLightManager.attachToGameWorld(this)
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
        onBlockPlacedListeners.forEach { listener ->
            listener.get()?.onBlockPlaced(value, x, y, layer)
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
        onBlockDestroyedListeners.forEach { listener ->
            listener.get()?.onBlockDestroyed(value, x, y, layer, withDrop, destroyedByPlayer)
        }
    }

    private fun setMap(x: Int, y: Int, layer: Layer, value: Block, dropOld: Boolean, destroyedByPlayer: Boolean) {
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

        gameWorldLightManager.update()

        box2dAccumulator += delta
        while (box2dAccumulator >= PHYSICS_STEP_DELTA) {
            world.step(PHYSICS_STEP_DELTA, 6, 2)
            box2dAccumulator -= PHYSICS_STEP_DELTA
        }
    }

    override fun dispose() {
        gameWorldLightManager.dispose()
        physicsController.dispose()
        gameWorldSolidBlockBodiesManager.dispose()
        world.dispose()
    }

    companion object {
        private const val TAG = "GameWorld"

        private const val PHYSICS_STEP_DELTA = 1f / 60f

        const val DAY_DURATION_SEC = 1200
    }
}
