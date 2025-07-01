package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.common.utils.BLOCK_SIZE_PX
import ru.fredboy.cavedroid.common.utils.bl
import ru.fredboy.cavedroid.common.utils.px
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BlockDamageAssetsRepositoryImpl @Inject constructor() : BlockDamageAssetsRepository() {

    private var blockDamageSprites: Array<Sprite>? = null

    override val damageStages: Int
        get() = requireNotNull(blockDamageSprites).size

    private fun loadBlockDamage() {
        val blockDamageTexture = loadTexture(BLOCK_DAMAGE_SHEET_PATH)
        val size = blockDamageTexture.width.bl
        val blockSize = BLOCK_SIZE_PX.toInt()

        blockDamageSprites = Array(size) { index ->
            flippedSprite(
                texture = blockDamageTexture,
                x = index.px.toInt(),
                y = 0,
                width = blockSize,
                height = blockSize,
            )
        }
    }

    override fun getBlockDamageSprite(stage: Int): Sprite = requireNotNull(blockDamageSprites)[stage]

    override fun initialize() {
        loadBlockDamage()
    }

    override fun dispose() {
        super.dispose()
        blockDamageSprites = null
    }

    companion object {
        private const val BLOCK_DAMAGE_SHEET_PATH = "textures/break.png"
    }
}
