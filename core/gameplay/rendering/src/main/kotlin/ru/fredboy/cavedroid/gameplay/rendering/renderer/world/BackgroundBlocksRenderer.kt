package ru.fredboy.cavedroid.gameplay.rendering.renderer.world

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import ru.fredboy.cavedroid.common.di.GameScope
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
class BackgroundBlocksRenderer @Inject constructor(
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

    override val background = true

    private val bgChunks = mutableMapOf<Pair<Int, Int>, ChunkFrameBuffer<RenderingTool.SpriteBatch>>()
    private val bgShadeChunks = mutableMapOf<Pair<Int, Int>, ChunkFrameBuffer<RenderingTool.ShapeRenderer>>()
    private val fgChunks = mutableMapOf<Pair<Int, Int>, ChunkFrameBuffer<RenderingTool.SpriteBatch>>()

    override fun draw(spriteBatch: SpriteBatch, shapeRenderer: ShapeRenderer, viewport: Rectangle, delta: Float) {
        drawChunks(
            spriteBatch = spriteBatch,
            viewport = viewport,
            chunks = bgChunks,
            chunkFactory = { x, y -> ChunkFrameBuffer(x, y, gameWorld, RenderingTool.SpriteBatch()) },
            drawFunction = { batch, x, y, drawX, drawY -> drawBackMap(batch.spriteBatch, x, y, drawX, drawY) },
        )

        drawBlockDamage(spriteBatch)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        drawChunks(
            spriteBatch = spriteBatch,
            viewport = viewport,
            chunks = bgShadeChunks,
            chunkFactory = { x, y ->
                ChunkFrameBuffer(
                    chunkX = x,
                    chunkY = y,
                    gameWorld = gameWorld,
                    renderingTool = RenderingTool.ShapeRenderer().apply {
                        this@apply.shapeRenderer.setColor(0f, 0f, 0f, .5f)
                    },
                )
            },
            drawFunction = { shaper, x, y, drawX, drawY ->
                shadeBackMap(shaper.shapeRenderer, x, y, drawX, drawY)
            },
        )

        Gdx.gl.glDisable(GL20.GL_BLEND)

        drawChunks(
            spriteBatch = spriteBatch,
            viewport = viewport,
            chunks = fgChunks,
            chunkFactory = { x, y -> ChunkFrameBuffer(x, y, gameWorld, RenderingTool.SpriteBatch()) },
            drawFunction = { batch, x, y, drawX, drawY -> drawForeMap(batch.spriteBatch, x, y, drawX, drawY) },
        )
    }

    override fun dispose() {
        super.dispose()
        bgChunks.forEach { (_, chunk) -> chunk.dispose() }
        bgShadeChunks.forEach { (_, chunk) -> chunk.dispose() }
        fgChunks.forEach { (_, chunk) -> chunk.dispose() }
        bgChunks.clear()
        bgShadeChunks.clear()
        fgChunks.clear()
    }

    companion object {
        private const val RENDER_LAYER = 100000
    }
}
