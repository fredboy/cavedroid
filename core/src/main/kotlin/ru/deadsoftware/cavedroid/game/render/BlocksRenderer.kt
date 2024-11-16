package ru.deadsoftware.cavedroid.game.render

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageFrameCountUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageSpriteUseCase
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.world.model.Layer
import ru.fredboy.cavedroid.entity.container.model.Furnace
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld

abstract class BlocksRenderer(
    protected val gameWorld: GameWorld,
    protected val mobsController: MobController,
    protected val containerController: ContainerController,
    protected val getBlockDamageFrameCount: GetBlockDamageFrameCountUseCase,
    protected val getBlockDamageSprite: GetBlockDamageSpriteUseCase,
) : IGameRenderer {

    protected abstract val background: Boolean

    private val Block.canSeeThrough
        get() = isNone() || params.isTransparent

    private fun blockDamageSprite(index: Int): Sprite? {
        if (index !in 0..< getBlockDamageFrameCount()) {
            return null
        }
        return getBlockDamageSprite[index]
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
                val furnace = containerController.getContainer(x, y, Layer.BACKGROUND.z) as? Furnace
                backgroundBlock.draw(spriteBatch, drawX, drawY, furnace?.isActive ?: false)
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
                val furnace = containerController.getContainer(x, y, Layer.FOREGROUND.z) as? Furnace
                foregroundBlock.draw(spriteBatch, drawX, drawY, furnace?.isActive ?: false)
            } else {
                foregroundBlock.draw(spriteBatch, drawX, drawY)
            }
        }
    }

    companion object {
        private const val MAX_BLOCK_DAMAGE_INDEX = 10
    }

}