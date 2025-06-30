package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = NewGameSurvivalAction.KEY)
class NewGameSurvivalAction @Inject constructor(
    private val gameController: ApplicationController,
) : IMenuAction {

    override fun perform() {
        gameController.newGameSurvival()
    }

    companion object {
        const val KEY = "new_game_survival_action"
    }
}