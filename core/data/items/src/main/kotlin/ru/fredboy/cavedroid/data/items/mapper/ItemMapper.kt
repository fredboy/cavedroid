package ru.fredboy.cavedroid.data.items.mapper

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.common.utils.colorFromHexString
import ru.fredboy.cavedroid.data.items.model.ItemDto
import ru.fredboy.cavedroid.data.items.repository.ItemsRepositoryImpl
import ru.fredboy.cavedroid.domain.assets.usecase.GetItemTextureUseCase
import ru.fredboy.cavedroid.domain.items.model.block.Block
import ru.fredboy.cavedroid.domain.items.model.item.CommonItemParams
import ru.fredboy.cavedroid.domain.items.model.item.Item
import ru.fredboy.cavedroid.domain.items.model.item.Item.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ItemMapper @Inject constructor(
    private val getItemTexture: GetItemTextureUseCase,
) {

    fun map(key: String, dto: ItemDto, block: Block?, slabTopBlock: Block.Slab?, slabBottomBlock: Block.Slab?): Item {
        val params = mapCommonParams(key, dto)

        return when (dto.type) {
            "normal" -> Normal(
                params = params,
                sprite = requireNotNull(loadSprite(dto))
            )

            "usable" -> Usable(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                useActionKey = requireNotNull(dto.actionKey)
            )

            "shovel" -> Shovel(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                mobDamageMultiplier = dto.mobDamageMultiplier,
                blockDamageMultiplier = dto.blockDamageMultiplier,
                level = requireNotNull(dto.toolLevel)
            )

            "sword" -> Sword(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                mobDamageMultiplier = dto.mobDamageMultiplier,
                blockDamageMultiplier = dto.blockDamageMultiplier,
                level = requireNotNull(dto.toolLevel)
            )

            "pickaxe" -> Pickaxe(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                mobDamageMultiplier = dto.mobDamageMultiplier,
                blockDamageMultiplier = dto.blockDamageMultiplier,
                level = requireNotNull(dto.toolLevel)
            )

            "axe" -> Axe(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                mobDamageMultiplier = dto.mobDamageMultiplier,
                blockDamageMultiplier = dto.blockDamageMultiplier,
                level = requireNotNull(dto.toolLevel)
            )

            "shears" -> Shears(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                mobDamageMultiplier = dto.mobDamageMultiplier,
                blockDamageMultiplier = dto.blockDamageMultiplier,
                level = requireNotNull(dto.toolLevel)
            )

            "block" -> Block(
                params = params,
                block = requireNotNull(block)
            )

            "slab" -> Slab(
                params = params,
                topPartBlock = requireNotNull(slabTopBlock),
                bottomPartBlock = requireNotNull(slabBottomBlock)
            )

            "food" -> Food(
                params = params,
                sprite = requireNotNull(loadSprite(dto)),
                heal = requireNotNull(dto.heal)
            )

            "none" -> None(
                params = params
            )

            else -> throw IllegalArgumentException("Unknown item type ${dto.type}")
        }
    }

    private fun mapCommonParams(key: String, dto: ItemDto): CommonItemParams {
        return CommonItemParams(
            key = key,
            name = dto.name,
            inHandSpriteOrigin = SpriteOrigin(
                x = dto.originX,
                y = dto.origin_y,
            ),
            maxStack = dto.maxStack,
            burningTimeMs = dto.burningTime,
            smeltProductKey = dto.smeltProduct,
        )
    }

    private fun loadSprite(dto: ItemDto): Sprite? {
        if (dto.type == "none" || dto.type == "block" || dto.texture == ItemsRepositoryImpl.FALLBACK_ITEM_KEY) {
            return null
        }

        val texture = getItemTexture[dto.texture]
        return Sprite(texture)
            .apply {
                flip(false, true)
                dto.tint?.let {
                    color = colorFromHexString(it)
                }
            }
    }

}