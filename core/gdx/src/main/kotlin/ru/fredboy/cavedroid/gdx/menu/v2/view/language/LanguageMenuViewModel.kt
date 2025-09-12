package ru.fredboy.cavedroid.gdx.menu.v2.view.language

import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.domain.items.repository.ItemsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import java.util.*

class LanguageMenuViewModel(
    private val applicationContextRepository: ApplicationContextRepository,
    private val navBackStack: NavBackStack,
    private val itemsRepository: ItemsRepository,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    val locales get() = applicationContextRepository.getSupportedLocales()

    fun onLanguageSelect(locale: Locale) {
        applicationContextRepository.setLocale(locale)
        itemsRepository.reload()
        navBackStack.pop()
    }

    fun onBackClick() {
        navBackStack.pop()
    }
}
