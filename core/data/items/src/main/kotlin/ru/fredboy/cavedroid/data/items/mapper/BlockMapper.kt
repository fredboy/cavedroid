package ru.fredboy.cavedroid.data.items.mapper

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.MathUtils
import ru.fredboy.cavedroid.data.items.model.BlockDto
import ru.fredboy.cavedroid.data.items.model.BlockLightDto
import ru.fredboy.cavedroid.data.items.model.DropAmountDto
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
import ru.fredboy.cavedroid.domain.items.model.block.BlockDropAmount
import ru.fredboy.cavedroid.domain.items.model.block.BlockDropInfo
import ru.fredboy.cavedroid.domain.items.model.block.BlockInsets
import ru.fredboy.cavedroid.domain.items.model.block.BlockLightInfo
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
            "ladder" -> Block.Ladder(commonBlockParams, dto.density ?: 0f)
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
        lightInfo = mapLightInfo(dto.lightInfo),
        allowAttachToNeighbour = dto.allowAttachToNeighbour,
        replaceable = dto.replaceable,
    )

    private fun mapLightInfo(info: BlockLightDto?): BlockLightInfo? {
        info ?: return null

        return BlockLightInfo(
            lightBrightness = MathUtils.clamp(info.lightBrightness, 0f, 1f),
            lightDistance = info.lightDistance,
        )
    }

    private fun mapToolType(dto: BlockDto): Class<out Item.Tool>? = when (dto.toolType) {
        "shovel" -> Item.Shovel::class.java
        "sword" -> Item.Sword::class.java
        "pickaxe" -> Item.Pickaxe::class.java
        "axe" -> Item.Axe::class.java
        "shears" -> Item.Shears::class.java

        else -> null
    }

    private fun mapBlockDropInfo(dto: BlockDto): List<BlockDropInfo> {
        return dto.dropInfo.mapNotNull { dropInfoDto ->
            val key = dropInfoDto.key.takeUnless { it == ItemsRepositoryImpl.FALLBACK_ITEM_KEY }
                ?: return@mapNotNull null
            val requiresTool = dropInfoDto.requiresTool

            val amount = when (dropInfoDto.amount) {
                is DropAmountDto.ExactAmount -> BlockDropAmount.ExactAmount(
                    amount = dropInfoDto.amount.amount,
                )

                is DropAmountDto.RandomChance -> BlockDropAmount.RandomChance(
                    chance = dropInfoDto.amount.chance,
                    amount = dropInfoDto.amount.amount,
                )

                is DropAmountDto.RandomRange -> BlockDropAmount.RandomRange(
                    range = dropInfoDto.amount.min..dropInfoDto.amount.max,
                    chance = dropInfoDto.amount.chance,
                )
            }

            BlockDropInfo(
                itemKey = key,
                requiresTool = requiresTool,
                amount = amount,
            )
        }
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
