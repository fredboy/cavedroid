package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.utils.WorldNameSanitizer
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class NewGameMenuViewModel(
    private val applicationController: ApplicationController,
    private val worldNameSanitizer: WorldNameSanitizer,
    private val navBackStack: NavBackStack,
) : ViewModel() {

    private fun createNewGameConfig(worldName: String, gameMode: GameMode): StartGameConfig.New {
        return StartGameConfig.New(
            worldName = worldName,
            saveDirectory = worldNameSanitizer.sanitizeWorldName(worldName),
            gameMode = gameMode,
        )
    }

    fun onSurvivalClick(worldName: String) {
        applicationController.startGame(createNewGameConfig(worldName, GameMode.SURVIVAL))
    }

    fun onCreativeClick(worldName: String) {
        applicationController.startGame(createNewGameConfig(worldName, GameMode.CREATIVE))
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
