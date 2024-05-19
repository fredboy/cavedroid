package ru.deadsoftware.cavedroid.game.mobs

import com.badlogic.gdx.math.MathUtils
import ru.deadsoftware.cavedroid.game.GameItemsHolder
import ru.deadsoftware.cavedroid.game.world.GameWorld

abstract class PeacefulMob(x: Float, y: Float, width: Float, height: Float, direction: Direction, maxHealth: Int, )
    : Mob(x, y, width, height, direction, Type.MOB, maxHealth) {

    override fun ai(world: GameWorld, gameItemsHolder: GameItemsHolder, mobsController: MobsController, delta: Float) {
        if (MathUtils.randomBoolean(delta)) {
            if (velocity.x != 0f) {
                velocity.x = 0f
            } else {
                changeDir()
            }
        }
    }

}