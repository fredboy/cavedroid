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

    fun attachToGameWorld(gameWorld: GameWorld) {
        _gameWorld = gameWorld
        initialize()
        gameWorld.addBlockPlacedListener(this)
    }

    protected abstract fun initialize()

    override fun dispose() {
        bodies.forEach { (_, body) -> body.world.destroyBody(body) }
        _bodies.clear()
        gameWorld.removeBlockPlacedListener(this)
        _gameWorld = null
    }
}
