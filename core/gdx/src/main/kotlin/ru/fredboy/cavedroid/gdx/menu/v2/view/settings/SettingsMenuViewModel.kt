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
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class SettingsMenuViewModel(
    private val applicationController: ApplicationController,
    private val navBackStack: NavBackStack,
    private val applicationContextRepository: ApplicationContextRepository,
) : ViewModel() {

    private val _screenScaleFlow = MutableSharedFlow<Int>(replay = 0)
    private val screenScaleFlow: Flow<Int> = _screenScaleFlow
        .onStart { emit(applicationContextRepository.getScreenScale()) }
        .distinctUntilChanged()

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

    val stateFlow: StateFlow<SettingsMenuState> = combine(
        screenScaleFlow,
        dynamicCameraFlow,
        fullscreenFlow,
        autoJumpFlow,
    ) { scale, dynamicCamera, fullscreen, autoJump ->
        SettingsMenuState(scale, dynamicCamera, fullscreen, autoJump)
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

    fun onChangeScreenScale(scale: Int) {
        viewModelScope.launch { _screenScaleFlow.emit(scale) }
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

    fun onDoneClick() {
        stateFlow.value.run {
            applicationContextRepository.setScreenScale(screenScale)
            applicationContextRepository.setFullscreen(fullscreen)
            applicationContextRepository.setUseDynamicCamera(dynamicCamera)
            applicationContextRepository.setAutoJumpEnabled(autoJump)
        }
        navBackStack.pop()
    }
}
