package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import com.badlogic.gdx.Gdx
import kotlinx.coroutines.CoroutineDispatcher
import ru.fredboy.cavedroid.common.mvvm.ViewModel
import java.util.*

abstract class BaseViewModel(
    private val dependencies: BaseViewModelDependencies,
) : ViewModel() {

    protected val ioDispatcher: CoroutineDispatcher get() = dependencies.dispatchers.io

    protected val backgroundDispatcher: CoroutineDispatcher get() = dependencies.dispatchers.background

    protected val mainDispatcher: CoroutineDispatcher get() = dependencies.dispatchers.main

    fun getLocalizedString(key: String, fallback: String = ""): String {
        return try {
            dependencies.fontAssetsRepository.getMenuLocalizationBundle().get(key)
        } catch (e: MissingResourceException) {
            Gdx.app.error(TAG, "Missing string with key '$key'", e)
            fallback
        }
    }

    fun getFormattedString(key: String, vararg args: String, fallback: String = ""): String {
        return try {
            dependencies.fontAssetsRepository.getMenuLocalizationBundle().format(key, *args)
        } catch (e: MissingResourceException) {
            Gdx.app.error(TAG, "Missing string with key '$key'", e)
            fallback
        }
    }

    fun playClickSound() {
        dependencies.soundPlayer.playUiSound(dependencies.uiSoundAssetsRepository.getClickSound())
    }

    companion object {
        private const val TAG = "BaseViewModel"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
