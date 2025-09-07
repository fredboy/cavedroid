package ru.fredboy.cavedroid.gdx.menu.v2.view.help

import ru.fredboy.cavedroid.domain.assets.repository.FontAssetsRepository
import ru.fredboy.cavedroid.gdx.menu.v2.navigation.NavBackStack
import ru.fredboy.cavedroid.gdx.menu.v2.view.about.AboutMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.attribution.AttributionMenuNavKey
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.notice.NoticeMenuNavKey

class HelpMenuViewModel(
    private val navBackStack: NavBackStack,
    fontAssetsRepository: FontAssetsRepository,
) : BaseViewModel(fontAssetsRepository) {

    fun onAboutClick() {
        navBackStack.push(AboutMenuNavKey)
    }

    fun onAttributionClick() {
        navBackStack.push(AttributionMenuNavKey)
    }

    fun onLicensesClick() {
        navBackStack.push(NoticeMenuNavKey)
    }

    fun onBackClicked() {
        navBackStack.pop()
    }
}
