package ru.deadsoftware.cavedroid.game.mobs

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.dto.SaveDataDto
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

    override fun getSaveData(): SaveDataDto.FallingBlockSaveData {
        return SaveDataDto.FallingBlockSaveData(
            version = SAVE_DATA_VERSION,
            x = x,
            y = y,
            width = width,
            height = height,
            velocityX = velocity.x,
            velocityY = velocity.y,
            type = mType,
            animDelta = mAnimDelta,
            anim = mAnim,
            direction = mDirection,
            dead = mDead,
            canJump = mCanJump,
            flyMode = mFlyMode,
            maxHealth = mMaxHealth,
            health = mHealth,
            blockKey = blockKey,
        )
    }

    companion object {
        private const val SAVE_DATA_VERSION = 1

        fun fromSaveData(saveData: SaveDataDto.FallingBlockSaveData): FallingBlock {
            saveData.verifyVersion(SAVE_DATA_VERSION)

            return FallingBlock(saveData.blockKey, saveData.x, saveData.y).apply {
                velocity.x = saveData.velocityX
                velocity.y = saveData.velocityY
                mAnimDelta = saveData.animDelta
                mAnim = saveData.anim
                mDirection = saveData.direction
                mDead = saveData.dead
                mCanJump = saveData.canJump
                mFlyMode = saveData.flyMode
                mMaxHealth = saveData.maxHealth
                mHealth = saveData.health
            }
        }
    }
}