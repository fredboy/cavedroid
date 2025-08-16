package ru.fredboy.cavedroid.gdx.menu.action

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.gdx.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = NewGameCreativeAction.KEY)
class NewGameCreativeAction @Inject constructor(
    private val gameController: ApplicationController,
) : IMenuAction {

    override fun perform() {
        gameController.newGameCreative()
    }

    companion object {
        const val KEY = "new_game_creative_action"
    }
}
