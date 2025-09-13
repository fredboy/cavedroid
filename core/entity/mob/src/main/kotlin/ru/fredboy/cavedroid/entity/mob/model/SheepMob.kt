package ru.fredboy.cavedroid.entity.mob.model

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import ru.fredboy.cavedroid.common.utils.applyOrigin
import ru.fredboy.cavedroid.common.utils.drawSprite
import ru.fredboy.cavedroid.domain.items.model.inventory.InventoryItem
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.usecase.GetItemByKeyUseCase

class SheepMob(
    params: MobParams,
    direction: Direction = Direction.random(),
) : WalkingMob(params, direction) {

    var hasFur: Boolean = true

    var woolToColor = "wool_colored_white" to Color.WHITE

    override fun getDropItems(itemByKey: GetItemByKeyUseCase): List<InventoryItem> {
        return if (hasFur) {
            super.getDropItems(itemByKey)
                .map { item ->
                    if (item.item.params.key.contains("wool")) {
                        itemByKey[woolToColor.first].toInventoryItem(item.amount)
                    } else {
                        item
                    }
                }
        } else {
            emptyList()
        }
    }

    override fun draw(
        spriteBatch: SpriteBatch,
        x: Float,
        y: Float,
        delta: Float,
    ) {
        updateAnimation(delta)

        val backgroundTintColor = tintColor.cpy().sub(Color(0xAAAAAA shl 8))

        params.sprites.filter { hasFur || !it.isOverlay }.forEach { spriteData ->
            val sprite = spriteData.sprite

            sprite.setFlip(looksRight(), sprite.isFlipY)
            sprite.applyOrigin(spriteData.origin.getFlipped(looksRight(), false))

            if (spriteData.isBackground) {
                sprite.color = backgroundTintColor
            } else {
                sprite.color = tintColor
            }

            val animationValue = if (spriteData.isStatic) {
                0f
            } else if (spriteData.isBackground) {
                anim
            } else {
                -anim
            }

            spriteBatch.drawSprite(
                sprite = sprite,
                x = x +
                    (width - sprite.width - spriteData.offsetX) * direction.index +
                    spriteData.offsetX * (1 - direction.index),
                y = y + spriteData.offsetY,
                rotation = animationValue,
                tint = woolToColor.second.takeIf { spriteData.isOverlay },
            )
        }
    }

    companion object {
        val FUR_COLORS_MAP = mapOf(
            "dye_white" to Pair("wool_colored_white", Color(1f, 1f, 1f, 1f)),
            "dye_orange" to Pair("wool_colored_orange", Color(0.93f, 0.57f, 0.16f, 1f)),
            "dye_magenta" to Pair("wool_colored_magenta", Color(0.85f, 0.37f, 0.85f, 1f)),
            "dye_light_blue" to Pair("wool_colored_light_blue", Color(0.39f, 0.58f, 0.93f, 1f)),
            "dye_yellow" to Pair("wool_colored_yellow", Color(0.95f, 0.86f, 0.29f, 1f)),
            "dye_lime" to Pair("wool_colored_lime", Color(0.48f, 0.83f, 0.31f, 1f)),
            "dye_pink" to Pair("wool_colored_pink", Color(0.96f, 0.63f, 0.76f, 1f)),
            "dye_gray" to Pair("wool_colored_gray", Color(0.25f, 0.25f, 0.25f, 1f)),
            "dye_light_gray" to Pair("wool_colored_light_gray", Color(0.57f, 0.57f, 0.57f, 1f)),
            "dye_cyan" to Pair("wool_colored_cyan", Color(0.28f, 0.66f, 0.66f, 1f)),
            "dye_purple" to Pair("wool_colored_purple", Color(0.58f, 0.33f, 0.58f, 1f)),
            "dye_blue" to Pair("wool_colored_blue", Color(0.23f, 0.34f, 0.67f, 1f)),
            "dye_brown" to Pair("wool_colored_brown", Color(0.43f, 0.27f, 0.18f, 1f)),
            "dye_green" to Pair("wool_colored_green", Color(0.33f, 0.51f, 0.27f, 1f)),
            "dye_red" to Pair("wool_colored_red", Color(0.67f, 0.22f, 0.22f, 1f)),
            "dye_black" to Pair("wool_colored_black", Color(0.10f, 0.10f, 0.10f, 1f)),
        )
    }
}
