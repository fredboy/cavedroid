package ru.fredboy.cavedroid.gdx.menu.v2.view.disclaimer

import ru.fredboy.cavedroid.common.api.AdController
import ru.fredboy.cavedroid.common.mvvm.NavBackStack
import ru.fredboy.cavedroid.domain.configuration.repository.ApplicationContextRepository
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModel
import ru.fredboy.cavedroid.gdx.menu.v2.view.common.BaseViewModelDependencies

class AdsDisclaimerViewModel(
    private val navBackStack: NavBackStack,
    private val applicationContextRepository: ApplicationContextRepository,
    private val adController: AdController,
    baseViewModelDependencies: BaseViewModelDependencies,
) : BaseViewModel(baseViewModelDependencies) {

    fun onAgreeClick() {
        applyConsent(true)
    }

    fun onOptOutClick() {
        applyConsent(false)
    }

    private fun applyConsent(consent: Boolean) {
        applicationContextRepository.setPersonalizedAdsConsent(consent)
        adController.setPersonalizedAdsEnabled(consent)
        navBackStack.pop()
    }
}
