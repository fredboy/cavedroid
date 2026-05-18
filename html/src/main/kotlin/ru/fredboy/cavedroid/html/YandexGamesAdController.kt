package ru.fredboy.cavedroid.html

import ru.fredboy.cavedroid.common.api.AdController

class YandexGamesAdController : AdController {

    override val supportsPersonalizedAdsConsent: Boolean = false

    override fun showBanner() = YandexGamesBridge.showBanner()

    override fun hideBanner() = Unit

    override fun loadInterstitial() = Unit

    override fun showInterstitial(onDismissed: () -> Unit) {
        YandexGamesBridge.showInterstitial { onDismissed() }
    }

    override fun setPersonalizedAdsEnabled(enabled: Boolean) = Unit

    override fun resume() = Unit

    override fun pause() = Unit

    override fun destroy() = Unit
}
