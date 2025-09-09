package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class SettingsMenuViewModel(
    private val navBackStack: NavBackStack,
    private val applicationContextRepository: ApplicationContextRepository,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val _dynamicCameraFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val dynamicCameraFlow: Flow<Boolean> = _dynamicCameraFlow
        .onStart { emit(applicationContextRepository.useDynamicCamera()) }
        .distinctUntilChanged()

    private val _fullscreenFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val fullscreenFlow: Flow<Boolean> = _fullscreenFlow
        .onStart { emit(applicationContextRepository.isFullscreen()) }
        .distinctUntilChanged()

    private val _autoJumpFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val autoJumpFlow: Flow<Boolean> = _autoJumpFlow
        .onStart { emit(applicationContextRepository.isAutoJumpEnabled()) }
        .distinctUntilChanged()

    private val _soundFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val soundFlow: Flow<Boolean> = _soundFlow
        .onStart { emit(applicationContextRepository.isSoundEnabled()) }
        .distinctUntilChanged()

    val stateFlow: StateFlow<SettingsMenuState> = combine(
        dynamicCameraFlow,
        fullscreenFlow,
        autoJumpFlow,
        soundFlow,
    ) { dynamicCamera, fullscreen, autoJump, sound ->
        SettingsMenuState(dynamicCamera, fullscreen, autoJump, sound)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500L),
        initialValue = createState(),
    )

    private fun createState(): SettingsMenuState {
        return SettingsMenuState(
            dynamicCamera = applicationContextRepository.useDynamicCamera(),
            fullscreen = applicationContextRepository.isFullscreen(),
            autoJump = applicationContextRepository.isAutoJumpEnabled(),
            sound = applicationContextRepository.isSoundEnabled(),
        )
    }

    fun onDynamicCameraClick(useDynamicCamera: Boolean) {
        viewModelScope.launch { _dynamicCameraFlow.emit(useDynamicCamera) }
    }

    fun onFullscreenClick(isFullscreen: Boolean) {
        viewModelScope.launch { _fullscreenFlow.emit(isFullscreen) }
    }

    fun onAutoJumpClick(autoJump: Boolean) {
        viewModelScope.launch { _autoJumpFlow.emit(autoJump) }
    }

    fun onSoundClick(enabled: Boolean) {
        viewModelScope.launch { _soundFlow.emit(enabled) }
    }

    fun onDoneClick() {
        stateFlow.value.run {
            applicationContextRepository.setFullscreen(fullscreen)
            applicationContextRepository.setUseDynamicCamera(dynamicCamera)
            applicationContextRepository.setAutoJumpEnabled(autoJump)
            applicationContextRepository.setSoundEnabled(sound)
        }
        navBackStack.pop()
    }
}
