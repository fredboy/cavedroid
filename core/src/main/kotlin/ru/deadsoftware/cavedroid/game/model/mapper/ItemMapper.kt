package ru.deadsoftware.cavedroid.game.model.mapper

import com.badlogic.gdx.graphics.g2d.Sprite
import dagger.Reusable
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.model.dto.ItemDto
import ru.deadsoftware.cavedroid.game.model.item.CommonItemParams
import ru.deadsoftware.cavedroid.game.model.item.Item
import ru.deadsoftware.cavedroid.game.model.item.Item.*
import ru.deadsoftware.cavedroid.misc.Assets
import ru.deadsoftware.cavedroid.misc.utils.AssetLoader
import ru.deadsoftware.cavedroid.misc.utils.SpriteOrigin
import ru.deadsoftware.cavedroid.misc.utils.colorFromHexString
import javax.inject.Inject

@Reusable
class ItemMapper @Inject constructor(
    private val assetLoader: AssetLoader,
) {

    fun map(key: String, dto: ItemDto, block: Block?, slabTopBlock: Block.Slab?, slabBottomBlock: Block.Slab?): Item {
        val params = mapCommonParams(key, dto)

        return when (dto.type) {
            "normal" -> Normal(params, requireNotNull(loadSprite(dto)))
            "usable" -> Usable(params, requireNotNull(loadSprite(dto)), requireNotNull(dto.actionKey))
            "shovel" -> Shovel(params, requireNotNull(loadSprite(dto)), dto.mobDamageMultiplier, dto.blockDamageMultiplier, requireNotNull(dto.toolLevel))
            "sword" -> Sword(params, requireNotNull(loadSprite(dto)), dto.mobDamageMultiplier, dto.blockDamageMultiplier, requireNotNull(dto.toolLevel))
            "pickaxe" -> Pickaxe(params, requireNotNull(loadSprite(dto)), dto.mobDamageMultiplier, dto.blockDamageMultiplier, requireNotNull(dto.toolLevel))
            "axe" -> Axe(params, requireNotNull(loadSprite(dto)), dto.mobDamageMultiplier, dto.blockDamageMultiplier, requireNotNull(dto.toolLevel))
            "shears" -> Shears(params, requireNotNull(loadSprite(dto)), dto.mobDamageMultiplier, dto.blockDamageMultiplier, requireNotNull(dto.toolLevel))
            "block" -> Block(params, requireNotNull(block))
            "slab" -> Slab(params, requireNotNull(slabTopBlock), requireNotNull(slabBottomBlock))
            "food" -> Food(params, requireNotNull(loadSprite(dto)), requireNotNull(dto.heal))
            "none" -> None(params)
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
        if (dto.type == "none" || dto.type == "block" || dto.texture == GameItemsHolder.FALLBACK_ITEM_KEY) {
            return null
        }

        val texture = Assets.resolveItemTexture(assetLoader, dto.texture)
        return Sprite(texture)
            .apply {
                flip(false, true)
                dto.tint?.let {
                    color = colorFromHexString(it)
                }
            }
    }

}