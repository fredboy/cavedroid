package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.stats.StatsMenuNavKey

class HelpMenuViewModel(
    private val navBackStack: NavBackStack,
    baseViewModelDependencies: BaseViewModelDependencies,
    private val applicationContextRepository: ApplicationContextRepository,
) : BaseViewModel(baseViewModelDependencies) {

    val isAboutButtonVisible: Boolean
        get() = !applicationContextRepository.isYandexGamesBuild()

    fun onAboutClick() {
        navBackStack.push(AboutMenuNavKey)
    }

    fun onAttributionClick() {
        navBackStack.push(AttributionMenuNavKey)
    }

    fun onLicensesClick() {
        navBackStack.push(NoticeMenuNavKey)
    }

    fun onStatisticsClick() {
        navBackStack.push(StatsMenuNavKey)
    }

    fun onBackClicked() {
        navBackStack.pop()
    }
}
