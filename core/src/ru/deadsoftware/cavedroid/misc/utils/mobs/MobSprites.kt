package ru.deadsoftware.cavedroid.misc.utils.mobs

import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.Mob.Direction
import ru.deadsoftware.cavedroid.misc.Assets

object MobSprites {

    object Player {

        fun getBackgroundHand() = Assets.playerSprite[1][2]

        fun getForegroundHand() = Assets.playerSprite[0][2]

        fun getBackgroundLeg() = Assets.playerSprite[1][3]

        fun getForegroundLeg() = Assets.playerSprite[0][3]

        fun getHead(direction: Mob.Direction) = Assets.playerSprite[direction.index][0]

        fun getBody(direction: Direction) = Assets.playerSprite[direction.index][1]

        fun getBodyRelativeX() = 2

        fun getBodyRelativeY() = 8

        fun getLegsRelativeY() = 20

    }

    object Pig {

        fun getForegroundLeg() = Assets.pigSprite[0][1]

        fun getBackgroundLeg() = Assets.pigSprite[1][1]

        fun getBody(direction: Direction) = Assets.pigSprite[direction.index][0]

        fun getLeftLegRelativeX(direction: Direction) = 9 - direction.index * 9

        fun getRightLegRelativeX(direction: Direction) = 21 - (9 * direction.index)

        fun getLegsRelativeY() = 12

    }

}