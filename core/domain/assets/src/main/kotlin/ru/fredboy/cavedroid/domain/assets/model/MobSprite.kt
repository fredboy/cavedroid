package ru.fredboy.cavedroid.domain.assets.model

import com.badlogic.gdx.graphics.g2d.Sprite
import ru.fredboy.cavedroid.common.utils.meters

sealed interface MobSprite {

    data class Player(
        val head: Sprite,
        val hand: Sprite,
        val body: Sprite,
        val leg: Sprite,
    ) : MobSprite {
        fun getBodyRelativeX() = 2.meters

        fun getBodyRelativeY() = 8.meters

        fun getLegsRelativeY() = 20.meters
    }

    data class Pig(
        val headAndBody: Sprite,
        val leg: Sprite,
    ) : MobSprite {

        fun getLeftLegRelativeX(directionIndex: Int) = (9 - directionIndex * 9).meters

        fun getRightLegRelativeX(directionIndex: Int) = (21 - (9 * directionIndex)).meters

        fun getLegsRelativeY() = 12.meters
    }

    data class Cow(
        val headAndBody: Sprite,
        val leg: Sprite,
    ) : MobSprite {

        fun getLeftLegRelativeX(directionIndex: Int) = (6 - directionIndex * 6).meters

        fun getRightLegRelativeX(directionIndex: Int) = (18 - (6 * directionIndex)).meters

        fun getLegsRelativeY() = 14.meters
    }
}
