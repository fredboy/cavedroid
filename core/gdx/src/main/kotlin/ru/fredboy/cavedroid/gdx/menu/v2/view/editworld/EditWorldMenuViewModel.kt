package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

import co.touchlab.kermit.Logger
import com.badlogic.gdx.graphics.Texture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.api.InlineTextInput
import ru.fredboy.cavedroid.common.api.SaveTransferController
import ru.fredboy.cavedroid.common.api.SoftKeyboardObserver
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.common.utils.WorldNameSanitizer
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.save.model.GameSaveDetails
import ru.fredboy.cavedroid.domain.save.repository.SaveDataRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class EditWorldMenuViewModel(
    private val applicationContextRepository: ApplicationContextRepository,
    private val saveDataRepository: SaveDataRepository,
    private val saveTransferController: SaveTransferController,
    private val worldNameSanitizer: WorldNameSanitizer,
    val inlineTextInput: InlineTextInput,
    softKeyboardObserver: SoftKeyboardObserver,
    private val navBackStack: NavBackStack,
    private val saveDirectory: String,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    val isExportSupported: Boolean get() = saveTransferController.isSupported

    private val loadedTextures = mutableListOf<Texture>()

    private var loadedDetails: SaveDetailsVo? = null

    var renameText: String = ""
    var cursorPosition: Int = 0

    private val mode = MutableStateFlow<Mode>(Mode.Loading)

    val stateFlow: StateFlow<EditWorldMenuState> =
        combine(mode, softKeyboardObserver.isVisible) { mode, keyboardUp ->
            when (mode) {
                is Mode.Loading -> EditWorldMenuState.Loading
                is Mode.ShowInfo -> EditWorldMenuState.ShowInfo(mode.details, mode.statusMessage)
                is Mode.Renaming -> EditWorldMenuState.Renaming(mode.currentName, keyboardUp)
                is Mode.ConfirmDelete -> EditWorldMenuState.ConfirmDelete(mode.worldName)
                is Mode.Working -> EditWorldMenuState.Working(mode.message)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = EditWorldMenuState.Loading,
        )

    override fun onShow() {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            mode.value = Mode.Loading
            try {
                val gameDir = applicationContextRepository.getGameDirectory()
                val details = withContext(ioDispatcher) {
                    saveDataRepository.getSaveDetails(gameDir, saveDirectory)
                }
                val vo = withContext(mainDispatcher) { details.toVo() }
                loadedDetails = vo
                mode.value = Mode.ShowInfo(vo)
            } catch (e: Exception) {
                logger.e(e) { "Failed to load save details for '$saveDirectory'" }
                navBackStack.pop()
            }
        }
    }

    fun onRenameClick() {
        val details = loadedDetails ?: return
        renameText = details.name
        cursorPosition = renameText.length
        mode.value = Mode.Renaming(details.name)
    }

    fun onWorldNameChanged(text: String, cursor: Int) {
        renameText = text
        cursorPosition = cursor
    }

    fun onConfirmRename(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            return
        }
        viewModelScope.launch {
            mode.value = Mode.Working(getLocalizedString("renaming"))
            withContext(ioDispatcher) {
                saveDataRepository.renameSave(
                    gameDataFolder = applicationContextRepository.getGameDirectory(),
                    saveDir = saveDirectory,
                    newName = trimmed,
                )
            }
            navBackStack.pop()
        }
    }

    fun onCancelRename() {
        loadedDetails?.let { mode.value = Mode.ShowInfo(it) }
    }

    fun onExportClick() {
        if (!saveTransferController.isSupported) {
            return
        }
        val details = loadedDetails ?: return
        viewModelScope.launch {
            mode.value = Mode.Working(getLocalizedString("exporting"))
            try {
                val zipBytes = withContext(ioDispatcher) {
                    saveDataRepository.exportSaveToZip(
                        gameDataFolder = applicationContextRepository.getGameDirectory(),
                        saveDir = saveDirectory,
                    )
                }
                saveTransferController.exportSave(
                    suggestedFileName = "${worldNameSanitizer.sanitizeWorldName(details.name)}.$SAVE_FILE_EXTENSION",
                    zipBytes = zipBytes,
                ) { success ->
                    viewModelScope.launch {
                        val message = getLocalizedString(if (success) "exportDone" else "exportFailed")
                        mode.value = Mode.ShowInfo(details, message)
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Failed to export save '$saveDirectory'" }
                mode.value = Mode.ShowInfo(details, getLocalizedString("exportFailed"))
            }
        }
    }

    fun onDeleteClick() {
        val details = loadedDetails ?: return
        mode.value = Mode.ConfirmDelete(details.name)
    }

    fun onConfirmDelete() {
        viewModelScope.launch {
            mode.value = Mode.Working(getLocalizedString("deletingWorld"))
            withContext(ioDispatcher) {
                saveDataRepository.deleteSave(
                    gameDataFolder = applicationContextRepository.getGameDirectory(),
                    saveDir = saveDirectory,
                )
            }
            navBackStack.pop()
        }
    }

    fun onCancelDelete() {
        loadedDetails?.let { mode.value = Mode.ShowInfo(it) }
    }

    fun onBackClick() {
        navBackStack.pop()
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

    private fun GameSaveDetails.toVo(): SaveDetailsVo {
        val screenshot = screenshotHandle
            ?.let { handle -> Texture(handle).also(loadedTextures::add) }

        return SaveDetailsVo(
            name = name,
            gameMode = gameMode,
            mapSize = "$widthBlocks × $heightBlocks",
            diskSize = formatSize(sizeBytes),
            version = version,
            isSupported = isSupported,
            seed = seed?.toString(),
            created = createdString,
            lastModified = lastModifiedString,
            screenshot = screenshot,
        )
    }

    private fun formatSize(bytes: Long): String {
        val mb = 1L shl 20
        val kb = 1L shl 10
        return when {
            bytes >= mb -> "${tenths(bytes, mb)} MB"
            bytes >= kb -> "${tenths(bytes, kb)} KB"
            else -> "$bytes B"
        }
    }

    private fun tenths(value: Long, unit: Long): String {
        val whole = value / unit
        val frac = (value % unit) * 10 / unit
        return "$whole.$frac"
    }

    private sealed interface Mode {
        data object Loading : Mode
        data class ShowInfo(val details: SaveDetailsVo, val statusMessage: String? = null) : Mode
        data class Renaming(val currentName: String) : Mode
        data class ConfirmDelete(val worldName: String) : Mode
        data class Working(val message: String) : Mode
    }

    companion object {
        private const val TAG = "EditWorldMenuViewModel"
        private const val SAVE_FILE_EXTENSION = "cvdworld"
        private val logger = Logger.withTag(TAG)
    }
}
