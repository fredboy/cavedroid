package ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig

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
import ru.fredboy.cavedroid.common.model.WorldSize
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.utils.WorldNameSanitizer
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class WorldConfigMenuViewModel(
    private val applicationController: ApplicationController,
    private val worldNameSanitizer: WorldNameSanitizer,
    private val navBackStack: NavBackStack,
    private val worldName: String,
    private val gameMode: GameMode,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val _stateFlow = MutableSharedFlow<WorldConfigMenuState>(replay = 0)

    val stateFlow = _stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WorldConfigMenuState.Show,
    )

    val worldSizes: List<WorldSize> = WorldSize.entries.toList()

    fun getWorldSizeLabel(size: WorldSize): String {
        return getLocalizedString("worldSize_${size.name.lowercase()}")
    }

    fun onSizeClick(size: WorldSize) {
        viewModelScope.launch {
            _stateFlow.emit(WorldConfigMenuState.Generating)
            delay(50)

            withContext(GdxMainDispatcher) {
                applicationController.startGame(
                    StartGameConfig.New(
                        worldName = worldName,
                        saveDirectory = worldNameSanitizer.sanitizeWorldName(worldName),
                        gameMode = gameMode,
                        worldSize = size,
                    ),
                )
            }
        }
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
