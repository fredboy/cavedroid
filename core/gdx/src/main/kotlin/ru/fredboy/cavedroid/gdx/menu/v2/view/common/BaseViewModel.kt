package ru.fredboy.cavedroid.gdx.menu.v2.view.common

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.ViewModel
import java.util.MissingResourceException

abstract class BaseViewModel(
    private val fontAssetsRepository: FontAssetsRepository,
) : ViewModel() {

    fun getLocalizedString(key: String, fallback: String = ""): String {
        return try {
            fontAssetsRepository.getMenuLocalizationBundle().get(key)
        } catch (e: MissingResourceException) {
            Gdx.app.error(TAG, "Missing string with key '$key'", e)
            fallback
        }
    }

    fun getFormattedString(key: String, vararg args: String, fallback: String = ""): String {
        return try {
            fontAssetsRepository.getMenuLocalizationBundle().format(key, *args)
        } catch (e: MissingResourceException) {
            Gdx.app.error(TAG, "Missing string with key '$key'", e)
            fallback
        }
    }

    companion object {
        private const val TAG = "BaseViewModel"
    }
}
