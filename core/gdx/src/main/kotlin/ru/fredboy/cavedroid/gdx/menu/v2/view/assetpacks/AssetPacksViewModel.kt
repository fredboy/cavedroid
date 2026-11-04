package ru.fredboy.cavedroid.gdx.menu.v2.view.assetpacks

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.api.ApplicationController
import ru.fredboy.cavedroid.common.api.AssetPackPlatform
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackFormat
import ru.fredboy.cavedroid.domain.assets.pack.AssetPackRepository
import ru.fredboy.cavedroid.domain.assets.pack.InstalledAssetPack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

data class AssetPacksState(
    val packs: List<InstalledAssetPack>,
    val selected: List<String>,
    val busy: Boolean = false,
    val message: String? = null,
    val confirmRemove: String? = null,
)

class AssetPacksViewModel(
    private val repository: AssetPackRepository,
    private val platform: AssetPackPlatform,
    private val backStack: NavBackStack,
    private val applicationController: ApplicationController,
    dependencies: BaseViewModelDependencies,
) : BaseViewModel(dependencies) {
    val state = MutableStateFlow(AssetPacksState(repository.installed(), repository.pendingIds, message = repository.startupError))
    val formatVersion = AssetPackFormat.VERSION
    val runningIds get() = repository.runningIds
    private val icons = mutableMapOf<String, Texture?>()
    private var disposed = false

    fun icon(pack: InstalledAssetPack): Texture? = icons.getOrPut(pack.id) {
        runCatching { Texture(pack.directory.child(pack.metadata.icon)) }.getOrNull()
    }

    fun toggle(pack: InstalledAssetPack) {
        val current = state.value
        if (current.busy) return
        when {
            pack.id in current.selected -> state.value = current.copy(selected = current.selected - pack.id)
            else -> state.value = current.copy(selected = listOf(pack.id) + current.selected)
        }
    }

    fun move(id: String, delta: Int) {
        val selected = state.value.selected.toMutableList()
        val index = selected.indexOf(id)
        val target = index + delta
        if (state.value.busy || index < 0 || target !in selected.indices) return
        selected.removeAt(index)
        selected.add(target, id)
        state.value = state.value.copy(selected = selected)
    }

    fun requestRemove(id: String) {
        if (!state.value.busy) state.value = state.value.copy(confirmRemove = id)
    }

    fun confirm(accepted: Boolean) {
        val current = state.value
        state.value = current.copy(confirmRemove = null)
        if (!accepted) return
        current.confirmRemove?.let { id ->
            work {
                repository.remove(id)
                state.value.copy(packs = repository.installed(), selected = state.value.selected - id)
            }
        }
    }

    fun importPack() {
        if (state.value.busy) return
        state.value = state.value.copy(busy = true, message = null)
        platform.pickArchive { result ->
            Gdx.app.postRunnable {
                if (disposed) {
                    result.getOrNull()?.delete()
                    return@postRunnable
                }
                val archive = result.getOrNull()
                if (archive == null) {
                    state.value = state.value.copy(busy = false, message = result.exceptionOrNull()?.message)
                } else {
                    work {
                        repository.importArchive(archive)
                        state.value.copy(packs = repository.installed())
                    }
                }
            }
        }
    }

    private fun work(action: () -> AssetPacksState) {
        state.value = state.value.copy(busy = true, message = null)
        viewModelScope.launch {
            try {
                val result = withContext(ioDispatcher) { action() }
                state.value = result.copy(busy = false)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                state.value = state.value.copy(busy = false, message = exception.message ?: getLocalizedString("packsFailed"))
            }
        }
    }

    fun done() {
        if (state.value.busy) return
        val selected = state.value.selected
        if (selected == runningIds) {
            runCatching { repository.saveSelection(selected) }
                .onSuccess { backStack.pop() }
                .onFailure { state.value = state.value.copy(message = it.message) }
            return
        }
        state.value = state.value.copy(busy = true, message = null)
        // Rebuild screens after the click event and current frame have finished using the old skin.
        Gdx.app.postRunnable {
            if (disposed) return@postRunnable
            runCatching { applicationController.applyAssetPacks(selected) }
                .onFailure { state.value = state.value.copy(busy = false, message = it.message ?: getLocalizedString("packsFailed")) }
        }
    }

    override fun onDispose() {
        disposed = true
        icons.values.filterNotNull().forEach(Texture::dispose)
        icons.clear()
    }
}
