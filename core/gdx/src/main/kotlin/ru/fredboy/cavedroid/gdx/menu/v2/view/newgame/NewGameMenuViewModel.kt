package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig.WorldConfigMenuNavKey

class NewGameMenuViewModel(
    private val navBackStack: NavBackStack,
    val inlineTextInput: InlineTextInput,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    var worldName: String = getLocalizedString("newWorld")

    private val _stateFlow = MutableSharedFlow<NewGameMenuState>(replay = 0)

    val stateFlow = _stateFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = NewGameMenuState.Show,
    )

    fun onSurvivalClick(worldName: String) {
        this.worldName = worldName
        navBackStack.push(WorldConfigMenuNavKey(worldName = worldName, gameMode = GameMode.SURVIVAL))
    }

    fun onCreativeClick(worldName: String) {
        this.worldName = worldName
        navBackStack.push(WorldConfigMenuNavKey(worldName = worldName, gameMode = GameMode.CREATIVE))
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
