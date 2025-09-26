package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import java.util.*

abstract class BaseViewModel(
    private val dependencies: BaseViewModelDependencies,
) : ViewModel() {

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
