package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.domain.assets.repository.FontTextureAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel

class DeleteWorldMenuViewModel(
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val navBackStack: NavBackStack,
    private val saveDirectory: String,
    worldName: String,
    fontAssetsRepository: FontTextureAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    private val _stateFlow = MutableSharedFlow<DeleteWorldMenuState>(replay = 0)

    val stateFlow = _stateFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = DeleteWorldMenuState.ConfirmDeleting(worldName),
        )

    fun onConfirmClick() {
        viewModelScope.launch {
            _stateFlow.emit(DeleteWorldMenuState.Deleting)
            withContext(Dispatchers.IO) {
                saveDataRepository.deleteSave(
                    gameDataFolder = applicationContextRepository.getGameDirectory(),
                    saveDir = saveDirectory,
                )

                withContext(Dispatchers.Default) {
                    navBackStack.pop()
                }
            }
        }
    }

    fun onCancelClick() {
        navBackStack.pop()
    }
}
