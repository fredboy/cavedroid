package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import co.touchlab.kermit.Logger
import com.badlogic.gdx.graphics.Texture
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld.DeleteWorldMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey
import kotlin.time.Duration.Companion.milliseconds

class SinglePlayerMenuViewModel(
    private val applicationContextRepository: ApplicationContextRepository,
    private val applicationController: ApplicationController,
    private val saveDataRepository: SaveDataRepository,
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    private val loadedTextures = mutableListOf<Texture>()

    private val reloadTrigger = MutableSharedFlow<Trigger>(replay = 0)

    private val saveInfoFlow = reloadTrigger
        .onStart { emit(Trigger.LOAD_LIST) }
        .flatMapConcat { trigger ->
            if (trigger == Trigger.LOADING_WORLD) {
                return@flatMapConcat flowOf(SinglePlayerMenuState.LoadingWorld)
            }

            if (trigger == Trigger.LOAD_FAILED) {
                return@flatMapConcat flowOf(SinglePlayerMenuState.LoadingFailed)
            }

            flow {
                emit(SinglePlayerMenuState.LoadingList)
                delay(100.milliseconds)

                val appDir = applicationContextRepository.getGameDirectory()
                val saves = withContext(ioDispatcher) {
                    saveDataRepository.getSavesInfo(appDir)
                }

                val list = saves.map { saveInfo ->
                    SaveInfoVo(
                        version = saveInfo.version,
                        name = saveInfo.name,
                        directory = saveInfo.directory,
                        timeCreated = saveInfo.lastModifiedString,
                        gameMode = saveInfo.gameMode,
                        isSupported = saveInfo.isSupported,
                        screenshot = saveInfo.screenshotHandle?.let { handle ->
                            withContext(mainDispatcher) {
                                Texture(handle).also { texture ->
                                    loadedTextures.add(
                                        texture,
                                    )
                                }
                            }
                        },
                    )
                }

                emit(SinglePlayerMenuState.ShowList(list))
            }
        }

    val stateFlow: StateFlow<SinglePlayerMenuState> =
        combine(reloadTrigger.onStart { emit(Trigger.LOAD_LIST) }, saveInfoFlow) { _, state ->
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SinglePlayerMenuState.LoadingList,
        )

    fun onNewGameClick() {
        navBackStack.push(NewGameMenuNavKey)
    }

    fun onLoadClick(save: SaveInfoVo) {
        viewModelScope.launch {
            reloadTrigger.emit(Trigger.LOADING_WORLD)

            // Two yields so the View's flow collector adds the LoadingWorld actors AND a
            // render pass paints them before startGame blocks the main thread.
            yield()
            yield()

            try {
                withContext(mainDispatcher) {
                    applicationController.startGame(
                        startGameConfig = StartGameConfig.Load(
                            worldName = save.name,
                            saveDirectory = save.directory,
                        ),
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to load world '${save.name}'" }
                reloadTrigger.emit(Trigger.LOAD_FAILED)
            }
        }
    }

    fun onLoadingFailedBackClick() {
        viewModelScope.launch {
            reloadTrigger.emit(Trigger.LOAD_LIST)
        }
    }

    fun onDeleteClick(save: SaveInfoVo) {
        navBackStack.push(DeleteWorldMenuNavKey(worldName = save.name, saveDirectory = save.directory))
    }

    fun onBackClick() {
        navBackStack.pop()
    }

    override fun onShow() {
        viewModelScope.launch {
            reloadTrigger.emit(Trigger.LOAD_LIST)
        }
    }

    override fun onHide() {
        disposeLoadedTextures()
    }

    override fun onDispose() {
        disposeLoadedTextures()
    }

    private fun disposeLoadedTextures() {
        loadedTextures.forEach(Texture::dispose)
        loadedTextures.clear()
    }

    private enum class Trigger {
        LOAD_LIST,
        LOADING_WORLD,
        LOAD_FAILED,
    }

    companion object {
        private const val TAG = "SinglePlayerMenuViewModel"
        private val logger = Logger.withTag(TAG)
    }
}
