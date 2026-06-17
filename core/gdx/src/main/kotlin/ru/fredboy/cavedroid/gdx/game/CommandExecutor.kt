package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.game.controller.mob.MobController
import javax.inject.Inject

@GameScope
class CommandExecutor @Inject constructor(
    private val mobController: MobController,
) {

    fun execute(commandWithArgs: String) {
        val commandWithArgs = commandWithArgs
            ?.trim()
            ?.split(' ')
            ?.takeIf(List<String>::isNotEmpty)
            ?: return

        val args = commandWithArgs.drop(1)
        when (commandWithArgs.first()) {
            "/tp" -> tp(args[0].toFloat(), args[1].toFloat())
        }
    }

    private fun tp(x: Float, y: Float) {
        mobController.player.apply {
            val transform = Vector2(
                x - position.x,
                y - position.y,
            )
            applyPendingTransform(transform)
        }
    }
}
