package ru.fredboy.cavedroid.data.items.mapper

import com.badlogic.gdx.graphics.Texture
import ru.fredboy.cavedroid.data.items.model.BlockDto
import ru.fredboy.cavedroid.data.items.repository.ItemsRepositoryImpl
import ru.fredboy.cavedroid.domain.assets.usecase.GetBlockTextureUseCase
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.block.Block.Chest
import ru.fredboy.cavedroid.domain.items.model.block.Block.Furnace
import ru.fredboy.cavedroid.domain.items.model.block.Block.Lava
import ru.fredboy.cavedroid.domain.items.model.block.Block.None
import ru.fredboy.cavedroid.domain.items.model.block.Block.Normal
import ru.fredboy.cavedroid.domain.items.model.block.Block.Slab
import ru.fredboy.cavedroid.domain.items.model.block.Block.Water
import ru.fredboy.cavedroid.domain.items.model.block.BlockAnimationInfo
import ru.fredboy.cavedroid.domain.items.model.block.BlockDropInfo
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.CommonBlockParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockMapper @Inject constructor(
    private val getBlockTexture: GetBlockTextureUseCase,
) {

    fun map(key: String, dto: BlockDto): Block {
        val commonBlockParams = mapCommonParams(key, dto)

        return when (dto.meta) {
            "water" -> Water(commonBlockParams, requireNotNull(dto.state), dto.density ?: 0f)
            "lava" -> Lava(commonBlockParams, requireNotNull(dto.state), dto.density ?: 0f)
            "slab" -> Slab(commonBlockParams, requireNotNull(dto.fullBlock), requireNotNull(dto.otherPart))
            "furnace" -> Furnace(commonBlockParams)
            "chest" -> Chest(commonBlockParams)
            "none" -> None(commonBlockParams)
            else -> Normal(commonBlockParams)
        }
    }

    private fun mapCommonParams(key: String, dto: BlockDto): CommonBlockParams = CommonBlockParams(
        key = key,
        collisionMargins = BlockInsets.Pixels(
            left = dto.left,
            top = dto.top,
            right = dto.right,
            bottom = dto.bottom,
        ).toMeters(),
        hitPoints = dto.hp,
        dropInfo = mapBlockDropInfo(dto),
        hasCollision = dto.collision,
        isBackground = dto.background,
        isTransparent = dto.transparent,
        requiresBlock = dto.blockRequired,
        animationInfo = mapBlockAnimationInfo(dto),
        texture = getTexture(dto.texture),
        spriteMargins = BlockInsets.Pixels(
            left = dto.spriteLeft,
            top = dto.spriteTop,
            right = dto.spriteRight,
            bottom = dto.spriteBottom,
        ),
        toolLevel = dto.toolLevel,
        toolType = mapToolType(dto),
        damage = dto.damage,
        tint = dto.tint,
        isFallable = dto.fallable,
        castsShadows = dto.castShadows,
    )

    private fun mapToolType(dto: BlockDto): Class<out Item.Tool>? = when (dto.toolType) {
        "shovel" -> Item.Shovel::class.java
        "sword" -> Item.Sword::class.java
        "pickaxe" -> Item.Pickaxe::class.java
        "axe" -> Item.Axe::class.java
        "shears" -> Item.Shears::class.java

        else -> null
    }

    private fun mapBlockDropInfo(dto: BlockDto): BlockDropInfo? {
        val drop = dto.drop
        val dropCount = dto.dropCount

        if (drop == ItemsRepositoryImpl.FALLBACK_ITEM_KEY || dropCount == 0) {
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

    private fun getTexture(textureName: String): Texture? {
        if (textureName == ItemsRepositoryImpl.FALLBACK_BLOCK_KEY) {
            return null
        }

        return getBlockTexture[textureName]
    }
}
