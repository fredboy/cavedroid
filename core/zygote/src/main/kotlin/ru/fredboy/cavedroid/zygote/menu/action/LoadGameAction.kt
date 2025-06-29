package ru.fredboy.cavedroid.zygote.menu.action

import ru.fredboy.cavedroid.common.api.GameController
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.domain.configuration.repository.GameContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.zygote.menu.action.annotation.BindsMenuAction
import javax.inject.Inject

@MenuScope
@BindsMenuAction(stringKey = LoadGameAction.KEY)
class LoadGameAction @Inject constructor(
    private val gameController: GameController,
    private val saveDataRepository: SaveDataRepository,
    private val gameContextRepository: GameContextRepository,
) : IMenuAction {

    override fun perform() {
        gameController.loadGame()
    }

    override fun canPerform(): Boolean {
        return saveDataRepository.exists(gameContextRepository.getGameDirectory())
    }

    companion object {
        const val KEY = "load_game_action"
    }
}