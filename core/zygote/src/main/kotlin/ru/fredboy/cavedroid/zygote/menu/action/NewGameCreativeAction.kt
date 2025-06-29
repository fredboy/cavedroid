package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.api.GameController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = NewGameCreativeAction.KEY)
class NewGameCreativeAction @Inject constructor(
    private val gameController: GameController,
) : IMenuAction {

    override fun perform() {
        gameController.newGameCreative()
    }

    companion object {
        const val KEY = "new_game_creative_action"
    }
}