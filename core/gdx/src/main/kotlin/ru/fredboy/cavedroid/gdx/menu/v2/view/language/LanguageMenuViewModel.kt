package ru.fredboy.cavedroid.gdx.menu.v2.view.language

import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import java.util.*

class LanguageMenuViewModel(
    private val applicationContextRepository: ApplicationContextRepository,
    private val navBackStack: NavBackStack,
    fontAssetsRepository: FontAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    val locales get() = applicationContextRepository.getSupportedLocales()

    fun onLanguageSelect(locale: Locale) {
        applicationContextRepository.setLocale(locale)
        navBackStack.pop()
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
