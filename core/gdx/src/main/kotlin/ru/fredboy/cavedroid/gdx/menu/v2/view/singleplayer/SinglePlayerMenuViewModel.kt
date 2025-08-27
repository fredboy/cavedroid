package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

import com.badlogic.gdx.graphics.Texture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.common.model.StartGameConfig
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.newgame.NewGameMenuNavKey

class SinglePlayerMenuViewModel(
    private val applicationContextRepository: ApplicationContextRepository,
    private val applicationController: ApplicationController,
    private val saveDataRepository: SaveDataRepository,
    private val navBackStack: NavBackStack,
) : ViewModel() {

    private val loadedTextures = mutableListOf<Texture>()

    private val reloadTrigger = MutableSharedFlow<Trigger>(replay = 0)

    private val saveInfoFlow = reloadTrigger
        .onStart { emit(Trigger.LOAD_LIST) }
        .map { trigger ->
            if (trigger == Trigger.LOADING_WORLD) {
                return@map SinglePlayerMenuState.LoadingWorld
            }

            val appDir = applicationContextRepository.getGameDirectory()
            val saves = withContext(Dispatchers.IO) {
                saveDataRepository.getSavesInfo(appDir)
            }

            saves.map { saveInfo ->
                SaveInfoVo(
                    version = saveInfo.version,
                    name = saveInfo.name,
                    directory = saveInfo.directory,
                    timeCreated = saveInfo.lastModifiedString,
                    gameMode = saveInfo.gameMode,
                    isSupported = saveInfo.isSupported,
                    screenshot = saveInfo.screenshotHandle?.let { handle ->
                        withContext(GdxMainDispatcher) {
                            Texture(handle).also { texture ->
                                loadedTextures.add(
                                    texture,
                                )
                            }
                        }
                    },
                )
            }.let { SinglePlayerMenuState.ShowList(it) }
        }

    val stateFlow: StateFlow<SinglePlayerMenuState> =
        combine(reloadTrigger.onStart { emit(Trigger.LOAD_LIST) }, saveInfoFlow) { _, state ->
            state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = SinglePlayerMenuState.ShowList(emptyList()),
        )

    fun onNewGameClick() {
        navBackStack.push(NewGameMenuNavKey)
    }

    fun onLoadClick(save: SaveInfoVo) {
        viewModelScope.launch {
            reloadTrigger.emit(Trigger.LOADING_WORLD)
            delay(50)

            withContext(GdxMainDispatcher) {
                applicationController.startGame(
                    startGameConfig = StartGameConfig.Load(
                        worldName = save.name,
                        saveDirectory = save.directory,
                    ),
                )
            }
        }
    }

    fun onDeleteClick(save: SaveInfoVo) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                saveDataRepository.deleteSave(
                    gameDataFolder = applicationContextRepository.getGameDirectory(),
                    saveDir = save.directory,
                )
            }
            reloadTrigger.emit(Trigger.LOAD_LIST)
        }
    }

    fun onBackClick() {
        navBackStack.pop()
    }

    override fun onDispose() {
        loadedTextures.forEach(Texture::dispose)
        loadedTextures.clear()
    }

    private enum class Trigger {
        LOAD_LIST,
        LOADING_WORLD,
    }
}
