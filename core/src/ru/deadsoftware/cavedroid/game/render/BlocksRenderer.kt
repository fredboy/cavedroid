package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.px

abstract class BlocksRenderer(
    protected val gameWorld: GameWorld,
    protected val mobsController: MobsController,
) : IGameRenderer {

    protected abstract val background: Boolean

    private val Block.canSeeThrough
        get() = isNone() || params.isTransparent

    private fun blockDamageSprite(index: Int): Sprite? {
        if (index !in 0..< MAX_BLOCK_DAMAGE_INDEX) {
            return null
        }
        return Assets.blockDamageSprites[index]
    }

    protected fun drawBlockDamage(spriteBatch: SpriteBatch, viewport: Rectangle) {
        val player = mobsController.player
        val blockDamage = player.blockDamage.takeIf { it > 0f } ?: return
        val cursorX = player.cursorX
        val cursorY = player.cursorY

        val block = if (background) {
            gameWorld.getBackMap(cursorX, cursorY)
        } else {
            gameWorld.getForeMap(cursorX, cursorY)
        }

        val index = (MAX_BLOCK_DAMAGE_INDEX.toFloat() * (blockDamage.toFloat() / block.params.hitPoints.toFloat()))
            .let(MathUtils::floor)
        val sprite = blockDamageSprite(index) ?: return

        if (gameWorld.hasForeAt(cursorX, cursorY) != background) {
            sprite.setBounds(
                /* x = */ cursorX.px - viewport.x + block.params.spriteMargins.left,
                /* y = */ cursorY.px - viewport.y + block.params.spriteMargins.top,
                /* width = */ block.spriteWidth,
                /* height = */ block.spriteHeight
            )
            sprite.draw(spriteBatch)
        }
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
            if (backgroundBlock is Block.Furnace) {
                backgroundBlock.draw(spriteBatch, drawX, drawY, gameWorld.getBackgroundFurnace(x, y)?.isActive ?: false)
            } else {
                backgroundBlock.draw(spriteBatch, drawX, drawY)
            }
        }
    }

    protected fun drawForeMap(spriteBatch: SpriteBatch, viewport: Rectangle, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)

        if (!foregroundBlock.isNone() && foregroundBlock.params.isBackground == background) {
            val drawX = x.px - viewport.x
            val drawY = y.px - viewport.y

            if (foregroundBlock is Block.Furnace) {
                foregroundBlock.draw(spriteBatch, drawX, drawY, gameWorld.getForegroundFurnace(x, y)?.isActive ?: false)
            } else {
                foregroundBlock.draw(spriteBatch, drawX, drawY)
            }
        }
    }

    companion object {
        private const val MAX_BLOCK_DAMAGE_INDEX = 10
    }

}