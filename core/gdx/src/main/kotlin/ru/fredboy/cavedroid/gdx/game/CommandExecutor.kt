package ru.fredboy.cavedroid.gdx.game

import com.badlogic.gdx.math.Vector2
import ru.fredboy.cavedroid.common.di.GameScope
import ru.fredboy.cavedroid.domain.world.model.Weather
import ru.fredboy.cavedroid.game.controller.mob.MobController
import ru.fredboy.cavedroid.game.world.GameWorld
import javax.inject.Inject

@GameScope
class CommandExecutor @Inject constructor(
    private val mobController: MobController,
    private val gameWorld: GameWorld,
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
            "/time" -> time(args[0].toFloat())
            "/weather" -> weather(args[0])
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

    private fun time(time: Float) {
        gameWorld.currentGameTime = time
    }

    private fun weather(weather: String) {
        gameWorld.weather = Weather.valueOf(weather.uppercase())
    }
}
