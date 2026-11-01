package ru.fredboy.cavedroid.gdx

import com.badlogic.gdx.scenes.scene2d.ui.Skin
import ru.fredboy.cavedroid.gdx.di.ApplicationComponent

internal fun initializeAssetResources(component: ApplicationComponent): Skin {
    component.packDefinitionValidator.validate()
    component.initializeAssets()
    component.itemsRepository.getAllItems()
    component.mobParamsRepository.getAllParams()
    return component.menuSkin
}

/** Keeps the current generation alive until both asset loading and activation succeed. */
internal fun reloadAssetPacks(
    current: ApplicationComponent,
    ids: List<String>,
    create: () -> ApplicationComponent,
    activate: (ApplicationComponent) -> Unit,
) {
    val repository = current.assetPackRepository
    val previousSelection = repository.pendingIds
    var candidate: ApplicationComponent? = null
    var skin: Skin? = null
    try {
        repository.saveSelection(ids)
        candidate = create()
        skin = initializeAssetResources(candidate)
        activate(candidate)
    } catch (exception: Exception) {
        runCatching { skin?.dispose() }.onFailure(exception::addSuppressed)
        candidate?.let { failed ->
            runCatching { failed.disposeAssets() }.onFailure(exception::addSuppressed)
            runCatching { failed.assetTextureLoader.disposeAll() }.onFailure(exception::addSuppressed)
        }
        runCatching { repository.saveSelection(previousSelection) }.onFailure(exception::addSuppressed)
        throw exception
    }
}
