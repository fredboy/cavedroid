package ru.fredboy.cavedroid.domain.assets.model

import com.badlogic.gdx.graphics.g2d.Sprite

sealed interface MobSprite {

    data class Player(
        val head: Sprite,
        val hand: Sprite,
        val body: Sprite,
        val leg: Sprite,
    ) : MobSprite {
        fun getBodyRelativeX() = .125f

        fun getBodyRelativeY() = .5f

        fun getLegsRelativeY() = 1.25f
    }

    data class Pig(
        val headAndBody: Sprite,
        val leg: Sprite,
    ) : MobSprite {

        fun getLeftLegRelativeX(directionIndex: Int) = .5625f - directionIndex * .5625f

        fun getRightLegRelativeX(directionIndex: Int) = 1.3125f - (.5625f * directionIndex)

        fun getLegsRelativeY() = .75f
    }
}
