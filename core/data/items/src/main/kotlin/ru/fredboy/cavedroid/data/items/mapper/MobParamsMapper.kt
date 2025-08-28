package ru.fredboy.cavedroid.data.items.mapper

import com.badlogic.gdx.graphics.g2d.Sprite
import dagger.Reusable
import ru.fredboy.cavedroid.common.model.SpriteOrigin
import ru.fredboy.cavedroid.common.utils.meters
import ru.fredboy.cavedroid.data.items.model.MobParamsDto
import ru.fredboy.cavedroid.data.items.model.MobSpriteDto
import ru.fredboy.cavedroid.domain.assets.GameAssetsHolder
import ru.fredboy.cavedroid.domain.items.model.mob.MobBehaviorType
import ru.fredboy.cavedroid.domain.items.model.mob.MobParams
import ru.fredboy.cavedroid.domain.items.model.mob.MobSprite
import javax.inject.Inject

@Reusable
class MobParamsMapper @Inject constructor(
    private val gameAssetsHolder: GameAssetsHolder,
    private val dropInfoMapper: DropInfoMapper,
) {

    fun map(key: String, dto: MobParamsDto): MobParams {
        return MobParams(
            name = dto.name,
            key = key,
            width = dto.width.meters,
            height = dto.height.meters,
            speed = dto.speed,
            behaviorType = mapBehaviorType(dto.type),
            dropInfo = dto.dropInfo.mapNotNull { dropInfoMapper.mapDropInfo(it) },
            hp = dto.hp,
            sprites = dto.sprites.map { spriteDto -> mapMobSprite(key, spriteDto) },
            animationRange = dto.animationRange,
            damageToPlayer = dto.damageToPlayer,
            takesSunDamage = dto.takesSunDamage,
        )
    }

    private fun mapBehaviorType(type: String): MobBehaviorType {
        return when (type) {
            "player" -> MobBehaviorType.PLAYER
            "falling_block" -> MobBehaviorType.FALLING_BLOCK
            "passive" -> MobBehaviorType.PASSIVE
            "neutral" -> MobBehaviorType.NEUTRAL
            "aggressive" -> MobBehaviorType.AGGRESSIVE

            else -> MobBehaviorType.PASSIVE
        }
    }

    private fun mapMobSprite(
        mobKey: String,
        dto: MobSpriteDto,
    ): MobSprite {
        val origin = SpriteOrigin(
            x = dto.originX,
            y = dto.originY,
        )

        val sprite = gameAssetsHolder.getMobTexture(mobKey, dto.file)
            .let { texture ->
                Sprite(texture).apply {
                    flip(false, true)
                }
            }

        return MobSprite(
            sprite = sprite,
            isBackground = dto.isBackground,
            isHead = dto.isHead,
            isHand = dto.isHand,
            isStatic = dto.isStatic,
            offsetX = dto.offsetX.meters,
            offsetY = dto.offsetY.meters,
            origin = origin,
        )
    }
}
