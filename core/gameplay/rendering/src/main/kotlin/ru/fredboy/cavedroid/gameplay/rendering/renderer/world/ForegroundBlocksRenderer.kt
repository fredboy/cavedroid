package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.common.utils.forEachBlockInArea
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageFrameCountUseCase
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockDamageSpriteUseCase
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.game.controller.container.ContainerController
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import ru.fredboy.cavedroid.gameplay.rendering.annotation.BindWorldRenderer
import ru.fredboy.cavedroid.gameplay.rendering.utils.ChunkFrameBuffer
import ru.fredboy.cavedroid.gameplay.rendering.utils.RenderingTool
import javax.inject.Inject

@GameScope
@BindWorldRenderer
class ForegroundBlocksRenderer @Inject constructor(
    gameWorld: GameWorld,
    mobController: MobController,
    containerController: ContainerController,
    getBlockDamageFrameCount: GetBlockDamageFrameCountUseCase,
    getBlockDamageSprite: GetBlockDamageSpriteUseCase,
    itemsRepository: ItemsRepository,
) : BlocksRenderer(
    gameWorld = gameWorld,
    mobsController = mobController,
    containerController = containerController,
    getBlockDamageFrameCount = getBlockDamageFrameCount,
    getBlockDamageSprite = getBlockDamageSprite,
    itemsRepository = itemsRepository,
) {

    override val renderLayer get() = RENDER_LAYER

    override val background = false

    private val chunks = mutableMapOf<Pair<Int, Int>, ChunkFrameBuffer<RenderingTool.SpriteBatch>>()

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        drawChunks(
            spriteBatch = spriteBatch,
            viewport = viewport,
            chunks = chunks,
            chunkFactory = { x, y -> ChunkFrameBuffer(x, y, gameWorld, RenderingTool.SpriteBatch()) },
            drawFunction = { batch, x, y, drawX, drawY -> drawForeMap(batch.spriteBatch, x, y, drawX, drawY) },
        )

        forEachBlockInArea(viewport) { x, y ->
            if (gameWorld.getForeMap(x, y).params.animationInfo != null) {
                drawForeMap(spriteBatch, x, y, x.toFloat(), y.toFloat())
            }
        }

        drawBlockDamage(spriteBatch)
    }

    override fun dispose() {
        super.dispose()
        chunks.forEach { (_, chunk) -> chunk.dispose() }
        chunks.clear()
    }

    companion object {
        private const val RENDER_LAYER = 100400
    }
}
