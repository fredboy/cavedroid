package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel

class DeleteWorldMenuViewModel(
    private val saveDataRepository: SaveDataRepository,
    private val applicationContextRepository: ApplicationContextRepository,
    private val navBackStack: NavBackStack,
    private val saveDirectory: String,
    private val worldName: String,
) : ViewModel() {

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
