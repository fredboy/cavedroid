package ru.deadsoftware.cavedroid.game.input.handler.keyboard

import com.badlogic.gdx.math.MathUtils
import ru.deadsoftware.cavedroid.MainConfig
import ru.deadsoftware.cavedroid.game.GameScope
import ru.deadsoftware.cavedroid.game.input.IGameInputHandler
import ru.deadsoftware.cavedroid.game.input.action.KeyboardInputAction
import ru.deadsoftware.cavedroid.game.input.action.keys.KeyboardInputActionKey
import ru.deadsoftware.cavedroid.game.mobs.MobsController
import ru.deadsoftware.cavedroid.game.mobs.Player
import ru.deadsoftware.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class MoveCursorControlsModeKeyboardInputHandler @Inject constructor(
    private val mainConfig: MainConfig,
    private val mobsController: MobsController,
    private val gameWorld: GameWorld,
) : IGameInputHandler<KeyboardInputAction> {

    override fun checkConditions(action: KeyboardInputAction): Boolean {
        return mainConfig.isTouch &&
                mobsController.player.controlMode == Player.ControlMode.CURSOR && action.isKeyDown &&
                (action.actionKey is KeyboardInputActionKey.Left ||
                action.actionKey is KeyboardInputActionKey.Right ||
                        action.actionKey is KeyboardInputActionKey.Jump ||
                        action.actionKey is KeyboardInputActionKey.Down)
    }

    private fun checkCursorBounds() {
        val player = mobsController.player
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

    override fun handle(action: KeyboardInputAction) {
        val player = mobsController.player

        when (action.actionKey) {
            KeyboardInputActionKey.Left -> player.cursorX--
            KeyboardInputActionKey.Right -> player.cursorX++
            KeyboardInputActionKey.Jump -> player.cursorY--
            KeyboardInputActionKey.Down -> player.cursorY++
            else -> return
        }

        checkCursorBounds()
    }

    companion object {
        private const val SURVIVAL_CURSOR_RANGE = 4
    }
}