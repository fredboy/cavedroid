package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageFrameCountUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageSpriteUseCase
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
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
    protected val itemsRepository: ItemsRepository,
) : IWorldRenderer {

    protected abstract val background: Boolean

    private val Block.canSeeThrough
        get() = isNone() || params.isTransparent

    private fun blockDamageSprite(index: Int): Sprite? {
        if (index !in 0..<getBlockDamageFrameCount()) {
            return null
        }
        return getBlockDamageSprite[index]
    }

    protected fun drawBlockDamage(spriteBatch: SpriteBatch) {
        val player = mobsController.player
        val blockDamage = player.blockDamage.takeIf { it > 0f } ?: return
        val selectedX = player.selectedX
        val selectedY = player.selectedY

        val block = if (background) {
            gameWorld.getBackMap(selectedX, selectedY)
        } else {
            gameWorld.getForeMap(selectedX, selectedY)
        }

        val index = (MAX_BLOCK_DAMAGE_INDEX.toFloat() * (blockDamage / block.params.hitPoints.toFloat()))
            .let(MathUtils::floor)
        val sprite = blockDamageSprite(index) ?: return

        if (gameWorld.hasForeAt(selectedX, selectedY) != background) {
            sprite.setBounds(
                /* x = */ selectedX.toFloat() + block.params.spriteMarginsMeters.left,
                /* y = */ selectedY.toFloat() + block.params.spriteMarginsMeters.top,
                /* width = */ block.spriteWidthMeters,
                /* height = */ block.spriteHeightMeters,
            )
            sprite.draw(spriteBatch)
        }
    }

    protected fun shadeBackMap(
        shapeRenderer: ShapeRenderer,
        x: Int,
        y: Int,
    ) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        val backgroundBlock = gameWorld.getBackMap(x, y)
        val isDefaultBackground = backgroundBlock.isNone() && y >= gameWorld.generatorConfig.seaLevel

        if (foregroundBlock.canSeeThrough && (!backgroundBlock.isNone() || isDefaultBackground)) {
            val shapeColor = shapeRenderer.color.cpy()

            if (isDefaultBackground) {
                shapeRenderer.color.apply { a = 0.75f }
            }

            val marginLeft = backgroundBlock.params.spriteMarginsMeters.left
            val marginTop = backgroundBlock.params.spriteMarginsMeters.top

            shapeRenderer.rect(
                /* x = */ x.toFloat() + marginLeft,
                /* y = */ y.toFloat() + marginTop,
                /* width = */ backgroundBlock.width,
                /* height = */ backgroundBlock.height,
            )

            shapeRenderer.color = shapeColor
        }
    }

    protected fun drawBackMap(spriteBatch: SpriteBatch, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)
        val backgroundBlock = gameWorld.getBackMap(x, y).let { block ->
            if (block.isNone() && y >= gameWorld.generatorConfig.seaLevel) {
                itemsRepository.getBlockByKey(gameWorld.generatorConfig.defaultBackgroundBlockKey)
            } else {
                block
            }
        }

        if (foregroundBlock.canSeeThrough && !backgroundBlock.isNone()) {
            if (backgroundBlock is Block.Furnace) {
                val furnace = containerController.getContainer(x, y, Layer.BACKGROUND.z) as? Furnace
                backgroundBlock.draw(spriteBatch, x.toFloat(), y.toFloat(), furnace?.isActive ?: false)
            } else {
                backgroundBlock.draw(spriteBatch, x.toFloat(), y.toFloat())
            }
        }
    }

    protected fun drawForeMap(spriteBatch: SpriteBatch, x: Int, y: Int) {
        val foregroundBlock = gameWorld.getForeMap(x, y)

        if (!foregroundBlock.isNone() && foregroundBlock.params.isBackground == background) {
            if (foregroundBlock is Block.Furnace) {
                val furnace = containerController.getContainer(x, y, Layer.FOREGROUND.z) as? Furnace
                foregroundBlock.draw(spriteBatch, x.toFloat(), y.toFloat(), furnace?.isActive ?: false)
            } else if (foregroundBlock.params.allowAttachToNeighbour) {
                drawAttachedToNeighbour(spriteBatch, foregroundBlock, x, y)
            } else {
                foregroundBlock.draw(spriteBatch, x.toFloat(), y.toFloat())
            }
        }
    }

    private fun drawAttachedToNeighbour(spriteBatch: SpriteBatch, block: Block, x: Int, y: Int) {
        val bottom = gameWorld.getForeMap(x, y + 1)
        val left = gameWorld.getForeMap(x - 1, y)
        val right = gameWorld.getForeMap(x + 1, y)

        if (bottom.params.hasCollision) {
            block.draw(spriteBatch, x.toFloat(), y.toFloat())
            return
        }

        val rotation = when {
            left.params.run { hasCollision && !isTransparent } -> 45f
            right.params.run { hasCollision && !isTransparent } -> -45f
            else -> 0f
        }

        spriteBatch.drawSprite(
            sprite = block.sprite,
            x = x.toFloat() + 0.6f * (rotation / -45f),
            y = y.toFloat(),
            rotation = rotation,
            origin = SpriteOrigin(0.5f, 1f),
        )
    }

    companion object {
        private const val MAX_BLOCK_DAMAGE_INDEX = 10
    }
}
