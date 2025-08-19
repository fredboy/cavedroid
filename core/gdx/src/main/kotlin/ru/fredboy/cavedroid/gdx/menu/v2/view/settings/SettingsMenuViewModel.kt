package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class SettingsMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    private val applicationContextRepository: ApplicationContextRepository,
) : ViewModel() {

    private val reloadTrigger = MutableSharedFlow<Unit>(replay = 0)

    val settingsMenuState: StateFlow<SettingsMenuState> = reloadTrigger
        .onStart { emit(Unit) }
        .map {
            createState()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500L),
            initialValue = createState(),
        )

    private fun createState(): SettingsMenuState {
        return SettingsMenuState(
            screenScale = applicationContextRepository.getScreenScale(),
            dynamicCamera = applicationContextRepository.useDynamicCamera(),
            fullscreen = applicationContextRepository.isFullscreen(),
            autoJump = applicationContextRepository.isAutoJumpEnabled(),
        )
    }

    fun onScreenScaleClick() {
        applicationContextRepository.setScreenScale((applicationContextRepository.getScreenScale() + 1) % 6 + 1)
        applicationController.triggerResize()

        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun onDynamicCameraClick() {
        applicationContextRepository.setUseDynamicCamera(!applicationContextRepository.useDynamicCamera())

        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun onFullscreenClick() {
        applicationContextRepository.setFullscreen(!applicationContextRepository.isFullscreen())

        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun onAutoJumpClick() {
        applicationContextRepository.setAutoJumpEnabled(!applicationContextRepository.isAutoJumpEnabled())

        viewModelScope.launch { reloadTrigger.emit(Unit) }
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
