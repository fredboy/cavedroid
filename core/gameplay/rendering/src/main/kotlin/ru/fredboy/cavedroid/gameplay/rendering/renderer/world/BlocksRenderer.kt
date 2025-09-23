package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
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
import ru.fredboy.cavedroid.gameplay.rendering.utils.ChunkFrameBuffer
import ru.fredboy.cavedroid.gameplay.rendering.utils.RenderingTool
import kotlin.math.floor

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
        drawX: Float,
        drawY: Float,
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
                /* x = */ drawX + marginLeft,
                /* y = */ drawY + marginTop,
                /* width = */ backgroundBlock.width,
                /* height = */ backgroundBlock.height,
            )

            shapeRenderer.color = shapeColor
        }
    }

    protected fun drawBackMap(spriteBatch: SpriteBatch, x: Int, y: Int, drawX: Float, drawY: Float) {
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
                backgroundBlock.draw(spriteBatch, drawX, drawY, furnace?.isActive ?: false)
            } else {
                backgroundBlock.draw(spriteBatch, drawX, drawY)
            }
        }
    }

    protected fun drawForeMap(spriteBatch: SpriteBatch, x: Int, y: Int, drawX: Float, drawY: Float) {
        val foregroundBlock = gameWorld.getForeMap(x, y)

        if (!foregroundBlock.isNone() && foregroundBlock.params.isBackground == background) {
            if (foregroundBlock is Block.Furnace) {
                val furnace = containerController.getContainer(x, y, Layer.FOREGROUND.z) as? Furnace
                foregroundBlock.draw(spriteBatch, drawX, drawY, furnace?.isActive ?: false)
            } else if (foregroundBlock.params.allowAttachToNeighbour) {
                drawAttachedToNeighbour(spriteBatch, foregroundBlock, x, y, drawX, drawY)
            } else {
                foregroundBlock.draw(spriteBatch, drawX, drawY)
            }
        }
    }

    private fun drawAttachedToNeighbour(
        spriteBatch: SpriteBatch,
        block: Block,
        x: Int,
        y: Int,
        drawX: Float,
        drawY: Float,
    ) {
        val bottom = gameWorld.getForeMap(x, y + 1)
        val left = gameWorld.getForeMap(x - 1, y)
        val right = gameWorld.getForeMap(x + 1, y)

        if (bottom.params.hasCollision) {
            block.draw(spriteBatch, drawX, drawY)
            return
        }

        val rotation = when {
            left.params.run { hasCollision && !isTransparent } -> 45f
            right.params.run { hasCollision && !isTransparent } -> -45f
            else -> 0f
        }

        spriteBatch.drawSprite(
            sprite = block.sprite,
            x = drawX + 0.6f * (rotation / -45f),
            y = drawY,
            rotation = rotation,
            origin = SpriteOrigin(0.5f, 1f),
        )
    }

    protected fun <Renderer : RenderingTool> drawChunks(
        spriteBatch: SpriteBatch,
        viewport: Rectangle,
        chunks: MutableMap<Pair<Int, Int>, ChunkFrameBuffer<Renderer>>,
        chunkFactory: (Int, Int) -> ChunkFrameBuffer<Renderer>,
        drawFunction: (Renderer, Int, Int, Float, Float) -> Unit,
    ) {
        val minChunkX = floor(viewport.x / ChunkFrameBuffer.CHUNK_SIZE).toInt()
        val maxChunkX = floor((viewport.x + viewport.width) / ChunkFrameBuffer.CHUNK_SIZE).toInt()
        val minChunkY = floor(viewport.y / ChunkFrameBuffer.CHUNK_SIZE).toInt()
        val maxChunkY = floor((viewport.y + viewport.height) / ChunkFrameBuffer.CHUNK_SIZE).toInt()

        for (cx in minChunkX..maxChunkX) {
            for (cy in minChunkY..maxChunkY) {
                val key = cx to cy
                val chunk = chunks.getOrPut(key) { chunkFactory(cx, cy) }

                chunk.render(
                    spriteBatch = spriteBatch,
                    drawFunction = drawFunction,
                )
            }
        }

        drawBlockDamage(spriteBatch)
    }

    companion object {
        private const val MAX_BLOCK_DAMAGE_INDEX = 10
    }
}
