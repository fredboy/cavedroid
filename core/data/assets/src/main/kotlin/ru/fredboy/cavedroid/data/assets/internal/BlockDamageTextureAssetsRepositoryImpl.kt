package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.common.utils.PIXELS_PER_METER
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.domain.assets.repository.BlockDamageTextureAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BlockDamageTextureAssetsRepositoryImpl @Inject constructor() : BlockDamageTextureAssetsRepository() {

    private var blockDamageSprites: Array<Sprite>? = null

    override val damageStages: Int
        get() = requireNotNull(blockDamageSprites).size

    private fun loadBlockDamage() {
        val blockDamageTexture = loadTexture(BLOCK_DAMAGE_SHEET_PATH)
        val size = blockDamageTexture.width.meters.toInt()
        val blockSize = PIXELS_PER_METER.toInt()

        blockDamageSprites = Array(size) { index ->
            flippedSprite(
                texture = blockDamageTexture,
                x = (index * PIXELS_PER_METER).toInt(),
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
