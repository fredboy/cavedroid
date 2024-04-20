package ru.deadsoftware.cavedroid.game.model.mapper

import com.badlogic.gdx.graphics.Texture
import dagger.Reusable
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.block.*
import ru.deadsoftware.cavedroid.game.model.block.Block.*
import ru.deadsoftware.cavedroid.game.model.dto.BlockDto
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import javax.inject.Inject

@Reusable
class BlockMapper @Inject constructor(
    private val assetLoader: AssetLoader,
) {

    fun map(key: String, dto: BlockDto): Block {
        val commonBlockParams = mapCommonParams(key, dto)

        return when (dto.meta) {
            "water" -> Water(commonBlockParams)
            "lava" -> Lava(commonBlockParams)
            "slab" -> Slab(commonBlockParams, requireNotNull(dto.fullBlock))
            else -> Normal(commonBlockParams)
        }
    }

    private fun mapCommonParams(key: String, dto: BlockDto): CommonBlockParams {
        return CommonBlockParams(
            id = dto.id,
            key = key,
            collisionMargins = BlockMargins(
                left = dto.left,
                top = dto.top,
                right = dto.right,
                bottom = dto.bottom
            ),
            hitPoints = dto.hp,
            dropInfo = mapBlockDropInfo(dto),
            hasCollision = dto.collision,
            isBackground = dto.background,
            isTransparent = dto.transparent,
            requiresBlock = dto.blockRequired,
            animationInfo = mapBlockAnimationInfo(dto),
            texture = loadTexture(dto.texture),
            spriteMargins = BlockMargins(
                left = dto.spriteLeft,
                top = dto.spriteTop,
                right = dto.spriteRight,
                bottom = dto.spriteBottom,
            )
        )
    }

    private fun mapBlockDropInfo(dto: BlockDto): BlockDropInfo? {
        val drop = dto.drop
        val dropCount = dto.dropCount

        if (drop == GameItemsHolder.FALLBACK_ITEM_KEY || dropCount == 0) {
            return null
        }

        return BlockDropInfo(
            itemKey = drop,
            count = dropCount,
        )
    }

    private fun mapBlockAnimationInfo(dto: BlockDto): BlockAnimationInfo? {
        if (!dto.animated) {
            return null
        }

        return BlockAnimationInfo(
            framesCount = dto.frames,
        )
    }

    private fun loadTexture(textureName: String): Texture? {
        if (textureName == GameItemsHolder.FALLBACK_BLOCK_KEY) {
            return null
        }

        return Texture(assetLoader.getAssetHandle("textures/blocks/$textureName.png"))
    }

}