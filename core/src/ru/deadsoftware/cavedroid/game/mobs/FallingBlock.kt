package ru.deadsoftware.cavedroid.game.mobs

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.utils.bl
import ru.deadsoftware.cavedroid.misc.utils.px

class FallingBlock(
    private val blockKey: String,
    x: Float,
    y: Float,
) : Mob(x, y, 1.px, 1.px, Direction.RIGHT, Type.FALLING_BLOCK, Int.MAX_VALUE) {

    @Transient
    private var _block: Block? = null

    init {
        velocity.y = 1f
    }

    override fun changeDir() = Unit

    override fun getSpeed() = 0f

    override fun jump() = Unit

    override fun ai(
        gameWorld: GameWorld,
        gameItemsHolder: GameItemsHolder,
        mobsController: MobsController,
        delta: Float
    ) {
        if (_block == null) {
            _block = gameItemsHolder.getBlock(blockKey)
        }

        if (velocity.isZero) {
            gameWorld.setForeMap(x.bl, y.bl, _block)
            kill()
        }
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float
    ) {
        _block?.draw(spriteBatch, x, y)
    }
}