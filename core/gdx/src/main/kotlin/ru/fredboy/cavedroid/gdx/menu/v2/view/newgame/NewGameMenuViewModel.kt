package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

import com.badlogic.gdx.utils.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.common.model.GameMode
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig.WorldConfigMenuNavKey

class NewGameMenuViewModel(
    private val navBackStack: NavBackStack,
    val inlineTextInput: InlineTextInput,
    softKeyboardObserver: SoftKeyboardObserver,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    var worldName: String = getLocalizedString("newWorld")
    var cursorPosition: Int = worldName.length

    // Pre-filled with a random numeric seed so default worlds stay randomized;
    // the field accepts any text (see resolveSeed).
    var enteredSeed: String = TimeUtils.millis().toString()
    var seedCursorPosition: Int = enteredSeed.length

    // Which field drives focus when the soft keyboard is raised (mobile).
    var editingSeed: Boolean = false

    // Web routes typing through InlineTextInput; the scene2d field never opens
    // the platform soft keyboard, so observer events are irrelevant and would
    // never fire anyway. Pin the state to "menu" mode for that path.
    val stateFlow: StateFlow<NewGameMenuState> = softKeyboardObserver.isVisible
        .map<Boolean, NewGameMenuState> { visible -> NewGameMenuState.Show(isKeyboardUp = visible) }
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = NewGameMenuState.Show(isKeyboardUp = false),
        )

    fun onWorldNameChanged(text: String, cursor: Int) {
        worldName = text
        cursorPosition = cursor
    }

    fun onSeedChanged(text: String, cursor: Int) {
        enteredSeed = text
        seedCursorPosition = cursor
    }

    fun onSurvivalClick(worldName: String) {
        this.worldName = worldName
        navBackStack.push(
            WorldConfigMenuNavKey(worldName = worldName, gameMode = GameMode.SURVIVAL, seed = resolveSeed()),
        )
    }

    fun onCreativeClick(worldName: String) {
        this.worldName = worldName
        navBackStack.push(
            WorldConfigMenuNavKey(worldName = worldName, gameMode = GameMode.CREATIVE, seed = resolveSeed()),
        )
    }

    private fun resolveSeed(): Long = enteredSeed.toLongOrNull() ?: enteredSeed.hashCode().toLong()

    fun onBackClick() {
        navBackStack.pop()
    }
}
