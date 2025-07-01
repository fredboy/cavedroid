package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = LoadGameAction.KEY)
class LoadGameAction @Inject constructor(
    private val applicationContextRepository: ApplicationContextRepository,
    private val gameController: ApplicationController,
    private val saveDataRepository: SaveDataRepository,
) : IMenuAction {

    override fun perform() {
        gameController.loadGame()
    }

    override fun canPerform(): Boolean {
        return saveDataRepository.exists(applicationContextRepository.getGameDirectory())
    }

    companion object {
        const val KEY = "load_game_action"
    }
}
