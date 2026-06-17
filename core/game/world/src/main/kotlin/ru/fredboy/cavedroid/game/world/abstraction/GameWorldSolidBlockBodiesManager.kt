package ru.fredboy.cavedroid.game.world.abstraction

import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.world.listener.OnBlockPlacedListener
import ru.fredboy.cavedroid.game.world.GameWorld

abstract class GameWorldSolidBlockBodiesManager :
    OnBlockPlacedListener,
    Disposable {

    private var _gameWorld: GameWorld? = null

    protected val gameWorld get() = requireNotNull(_gameWorld)

    protected val world get() = gameWorld.world

    protected val _bodies = mutableMapOf<Pair<Int, Int>, Body>()

    val bodies: Map<Pair<Int, Int>, Body> get() = _bodies

    private val bodiesReadyListeners = mutableListOf<ChunkBodiesReadyListener>()

    fun attachToGameWorld(gameWorld: GameWorld) {
        _gameWorld = gameWorld
        initialize()
        gameWorld.addBlockPlacedListener(this)
    }

    protected abstract fun initialize()

    fun addChunkBodiesReadyListener(listener: ChunkBodiesReadyListener) {
        bodiesReadyListeners.add(listener)
    }

    fun removeChunkBodiesReadyListener(listener: ChunkBodiesReadyListener) {
        bodiesReadyListeners.remove(listener)
    }

    protected fun notifyChunkBodiesReady(chunkX: Int) {
        bodiesReadyListeners.toList().forEach { it.onChunkBodiesReady(chunkX) }
    }

    override fun dispose() {
        bodies.forEach { (_, body) -> body.world.destroyBody(body) }
        _bodies.clear()
        bodiesReadyListeners.clear()
        gameWorld.removeBlockPlacedListener(this)
        _gameWorld = null
    }
}

fun interface ChunkBodiesReadyListener {
    fun onChunkBodiesReady(chunkX: Int)
}
