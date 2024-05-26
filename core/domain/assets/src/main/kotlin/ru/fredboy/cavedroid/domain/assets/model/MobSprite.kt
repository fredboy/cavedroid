package ru.fredboy.cavedroid.domain.assets.model

import com.badlogic.gdx.graphics.g2d.Sprite

sealed interface MobSprite {

    data class Player(
        val head: Sprite,
        val hand: Sprite,
        val body: Sprite,
        val leg: Sprite,
    ) : MobSprite {
        fun getBodyRelativeX() = 2

        fun getBodyRelativeY() = 8

        fun getLegsRelativeY() = 20
    }

    data class Pig(
        val headAndBody: Sprite,
        val leg: Sprite
    ) : MobSprite {

        fun getLeftLegRelativeX(directionIndex: Int) = 9 - directionIndex * 9

        fun getRightLegRelativeX(directionIndex: Int) = 21 - (9 * directionIndex)

        fun getLegsRelativeY() = 12

    }

}