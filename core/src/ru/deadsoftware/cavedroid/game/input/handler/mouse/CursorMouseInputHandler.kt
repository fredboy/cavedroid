package ru.deadsoftware.cavedroid.game.input.handler.mouse

import com.badlogic.gdx.math.MathUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.MouseInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.MouseInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.Mob
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.Player
import ru.deadsoftware.cavedroid.game.model.block.Block
import ru.deadsoftware.cavedroid.game.world.GameWorld
import ru.deadsoftware.cavedroid.misc.utils.bl
import ru.deadsoftware.cavedroid.misc.utils.px
import javax.inject.Inject

@GameScope
class CursorMouseInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
) : IGameInputHandler<MouseInputAction> {

    private val player get() = mobsController.player

    private val Block.isAutoselectable
        get() = !isNone() && params.hasCollision

    private fun GameWorld.isCurrentBlockAutoselectable() =
        getForeMap(player.cursorX, player.cursorY).isAutoselectable

    private fun checkCursorBounds() {
        if (player.gameMode == 0) {
            val minCursorX = player.mapX - SURVIVAL_CURSOR_RANGE
            val maxCursorX = player.mapX + SURVIVAL_CURSOR_RANGE
            val minCursorY = player.middleMapY - SURVIVAL_CURSOR_RANGE
            val maxCursorY = player.middleMapY + SURVIVAL_CURSOR_RANGE

            player.cursorX = MathUtils.clamp(player.cursorX, minCursorX, maxCursorX)
            player.cursorY = MathUtils.clamp(player.cursorY, minCursorY, maxCursorY)
        }

        player.cursorY = MathUtils.clamp(player.cursorY, 0, gameWorld.height - 1)
    }

    private fun setPlayerDirectionToCursor() {
        if (player.controlMode != Player.ControlMode.CURSOR) {
            return
        }

        if (player.cursorX.px + 8 < player.x + player.width / 2) {
            player.setDir(Mob.Direction.LEFT)
        } else {
            player.setDir(Mob.Direction.RIGHT)
        }
    }

    private fun handleWalkTouch() {
        player.cursorX = player.mapX + player.direction.basis
        player.cursorY = player.upperMapY

        for (i in 1..2) {
            if (gameWorld.isCurrentBlockAutoselectable()) {
                break
            }
            player.cursorY++
        }

        if (!gameWorld.isCurrentBlockAutoselectable()) {
            player.cursorX -= player.direction.basis
        }
    }

    private fun getPlayerHeadRotation(mouseWorldX: Float, mouseWorldY: Float): Float {
        val h = mouseWorldX - player.x
        val v = mouseWorldY - player.y

        return MathUtils.atan(v / h) * MathUtils.radDeg
    }

    private fun handleMouse(action: MouseInputAction) {
        val worldX = action.screenX + action.cameraViewport.x
        val worldY = action.screenY + action.cameraViewport.y

        // when worldX < 0, need to subtract 1 to avoid negative zero
//        val fixCycledWorld = if (worldX < 0) 1 else 0

        player.cursorX = worldX.bl - 0
        player.cursorY = worldY.bl

        player.headRotation = getPlayerHeadRotation(worldX, worldY)
    }

    override fun checkConditions(action: MouseInputAction): Boolean {
        return action.actionKey is MouseInputActionKey.None
    }

    override fun handle(action: MouseInputAction) {
        val pastCursorX = player.cursorX
        val pastCursorY = player.cursorY

        when {
            player.controlMode == Player.ControlMode.WALK && mainConfig.isTouch -> handleWalkTouch()
            !mainConfig.isTouch -> handleMouse(action)
        }

        checkCursorBounds()
        setPlayerDirectionToCursor()

        if (player.cursorX != pastCursorX || player.cursorY != pastCursorY) {
            player.blockDamage = 0f
        }
    }

    companion object {
        private const val SURVIVAL_CURSOR_RANGE = 4
    }

}