package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
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

    private val _stateFlow = MutableSharedFlow<NewGameMenuState>(replay = 0)

    val stateFlow = _stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = NewGameMenuState.Show,
    )

    private fun createNewGameConfig(worldName: String, gameMode: GameMode): StartGameConfig.New {
        return StartGameConfig.New(
            worldName = worldName,
            saveDirectory = worldNameSanitizer.sanitizeWorldName(worldName),
            gameMode = gameMode,
        )
    }

    fun onSurvivalClick(worldName: String) {
        viewModelScope.launch {
            _stateFlow.emit(NewGameMenuState.Generating)
            delay(50)

            withContext(GdxMainDispatcher) {
                applicationController.startGame(createNewGameConfig(worldName, GameMode.SURVIVAL))
            }
        }
    }

    fun onCreativeClick(worldName: String) {
        viewModelScope.launch {
            _stateFlow.emit(NewGameMenuState.Generating)
            delay(50)

            withContext(GdxMainDispatcher) {
                applicationController.startGame(createNewGameConfig(worldName, GameMode.CREATIVE))
            }
        }
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
