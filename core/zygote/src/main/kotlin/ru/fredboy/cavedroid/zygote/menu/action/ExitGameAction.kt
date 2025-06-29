package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.api.GameController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = ExitGameAction.KEY)
class ExitGameAction @Inject constructor(
    private val gameController: GameController,
) : IMenuAction {

    override fun perform() {
        gameController.exitGame()
    }

    companion object {
        const val KEY = "exit_game_action"
    }
}