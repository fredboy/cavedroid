package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.configuration.model.LightingBackend
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class SettingsMenuViewModel(
    private val navBackStack: NavBackStack,
    private val applicationContextRepository: ApplicationContextRepository,
    private val adController: AdController,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val showPersonalizedAdsToggle: Boolean
        get() = adController.supportsPersonalizedAdsConsent

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

    private val _smoothLightingFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val smoothLightingFlow: Flow<Boolean> = _smoothLightingFlow
        .onStart { emit(applicationContextRepository.getLightingBackend() == LightingBackend.BFS) }
        .distinctUntilChanged()

    private val _personalizedAdsFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val personalizedAdsFlow: Flow<Boolean> = _personalizedAdsFlow
        .onStart { emit(applicationContextRepository.getPersonalizedAdsConsent() ?: false) }
        .distinctUntilChanged()

    private val _canResetHintsFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val canResetHintsFlow: Flow<Boolean> = _canResetHintsFlow
        .onStart { emit(computeCanResetHints()) }
        .distinctUntilChanged()

    private val _showDebugInfoFlow = MutableSharedFlow<Boolean>(replay = 0)
    private val showDebugInfoFlow: Flow<Boolean> = _showDebugInfoFlow
        .onStart { emit(applicationContextRepository.preferShowDebug) }
        .distinctUntilChanged()

    val stateFlow: StateFlow<SettingsMenuState> = combine(
        dynamicCameraFlow,
        fullscreenFlow,
        autoJumpFlow,
        soundFlow,
        smoothLightingFlow,
        personalizedAdsFlow,
        canResetHintsFlow,
        showDebugInfoFlow,
    ) { values ->
        SettingsMenuState(
            dynamicCamera = values[0],
            fullscreen = values[1],
            autoJump = values[2],
            sound = values[3],
            smoothLighting = values[4],
            showPersonalizedAdsToggle = showPersonalizedAdsToggle,
            personalizedAds = values[5],
            showFullscreenButton = Gdx.graphics.supportsDisplayModeChange() &&
                    Gdx.app.type != Application.ApplicationType.WebGL,
            canResetHints = values[6],
            showDebugSetting = applicationContextRepository.isDebug(),
            preferShowDebug = values[7],
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500L),
        initialValue = createState(),
    )

    private fun computeCanResetHints(): Boolean {
        return applicationContextRepository.isOnboardingShown() ||
                applicationContextRepository.isInventoryHintShown()
    }

    private fun createState(): SettingsMenuState {
        return SettingsMenuState(
            dynamicCamera = applicationContextRepository.useDynamicCamera(),
            fullscreen = applicationContextRepository.isFullscreen(),
            autoJump = applicationContextRepository.isAutoJumpEnabled(),
            sound = applicationContextRepository.isSoundEnabled(),
            smoothLighting = applicationContextRepository.getLightingBackend() == LightingBackend.BFS,
            showPersonalizedAdsToggle = showPersonalizedAdsToggle,
            personalizedAds = applicationContextRepository.getPersonalizedAdsConsent() ?: false,
            showFullscreenButton = Gdx.graphics.supportsDisplayModeChange() &&
                    Gdx.app.type != Application.ApplicationType.WebGL,
            canResetHints = computeCanResetHints(),
            showDebugSetting = applicationContextRepository.isDebug(),
            preferShowDebug = applicationContextRepository.preferShowDebug,
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

    fun onSmoothLightingClick(enabled: Boolean) {
        viewModelScope.launch { _smoothLightingFlow.emit(enabled) }
    }

    fun onPersonalizedAdsClick(enabled: Boolean) {
        viewModelScope.launch { _personalizedAdsFlow.emit(enabled) }
    }

    fun onShowDebugClick(enabled: Boolean) {
        viewModelScope.launch { _showDebugInfoFlow.emit(enabled) }
    }

    fun onDoneClick() {
        stateFlow.value.run {
            applicationContextRepository.setFullscreen(fullscreen)
            applicationContextRepository.setUseDynamicCamera(dynamicCamera)
            applicationContextRepository.setAutoJumpEnabled(autoJump)
            applicationContextRepository.setSoundEnabled(sound)
            applicationContextRepository.setLightingBackend(
                if (smoothLighting) LightingBackend.BFS else LightingBackend.LEGACY,
            )
            if (showPersonalizedAdsToggle) {
                applicationContextRepository.setPersonalizedAdsConsent(personalizedAds)
                adController.setPersonalizedAdsEnabled(personalizedAds)
            }
            applicationContextRepository.preferShowDebug = preferShowDebug
        }
        navBackStack.pop()
    }

    fun onResetHintsClick() {
        applicationContextRepository.setOnboardingShown(false)
        applicationContextRepository.setInventoryHintShown(false)
        viewModelScope.launch { _canResetHintsFlow.emit(false) }
    }
}
