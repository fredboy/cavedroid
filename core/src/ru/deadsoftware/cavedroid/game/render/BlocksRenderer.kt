package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.GameInput
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.px

abstract class BlocksRenderer(
    protected val gameWorld: GameWorld,
    protected val gameInput: GameInput,
) : IGameRenderer {

    protected abstract val background: Boolean

    private val Block.canSeeThrough
        get() = isNone() || params.isTransparent

    private fun blockDamageTexture(index: Int): TextureRegion? {
        if (index !in 0..MAX_BLOCK_DAMAGE_INDEX) {
            return null
        }
        val textureKey = "$BLOCK_DAMAGE_TEXTURE_PREFIX$index"
        return Assets.textureRegions[textureKey]
    }

    private fun drawBlockDamage(spriteBatch: SpriteBatch, block: Block, x: Float, y: Float) {
        val blockDamage = gameInput.blockDamage
        if (blockDamage <= 0) {
            return
        }

        val index = MAX_BLOCK_DAMAGE_INDEX * (blockDamage / block.params.hitPoints)
        val texture = blockDamageTexture(index) ?: return

        spriteBatch.draw(texture, x, y)
    }

    protected fun shadeBackMap(
        shapeRenderer: ShapeRenderer,
        viewport: Rectangle,
        x: Int,
        y: Int
    ) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        val backgroundBlock = gameWorld.getBackMap(x, y)

        if (foregroundBlock.canSeeThrough && !backgroundBlock.isNone()) {
            val drawX = x.px - viewport.x
            val drawY = y.px - viewport.y
            val marginLeft = backgroundBlock.params.spriteMargins.left
            val marginTop = backgroundBlock.params.spriteMargins.top

            shapeRenderer.rect(
                /* x = */ drawX + marginLeft,
                /* y = */ drawY + marginTop,
                /* width = */ backgroundBlock.width,
                /* height = */ backgroundBlock.height
            )
        }
    }

    protected fun drawBackMap(spriteBatch: SpriteBatch, viewport: Rectangle, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        val backgroundBlock = gameWorld.getBackMap(x, y)

        if (foregroundBlock.canSeeThrough && !backgroundBlock.isNone()) {
            val drawX = x.px - viewport.x
            val drawY = y.px - viewport.y
            backgroundBlock.draw(spriteBatch, drawX, drawY)

            if (foregroundBlock.isNone()) {
                drawBlockDamage(spriteBatch, backgroundBlock, drawX, drawY)
            }
        }
    }

    protected fun drawForeMap(spriteBatch: SpriteBatch, viewport: Rectangle, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)

        if (!foregroundBlock.isNone() && foregroundBlock.params.isBackground == background) {
            val drawX = x.px - viewport.x
            val drawY = y.px - viewport.y
            foregroundBlock.draw(spriteBatch, drawX, drawY)

            if (!foregroundBlock.isNone()) {
                drawBlockDamage(spriteBatch, foregroundBlock, drawX, drawY)
            }
        }
    }

    companion object {
        private const val BLOCK_DAMAGE_TEXTURE_PREFIX = "break_"
        private const val MAX_BLOCK_DAMAGE_INDEX = 10
    }

}