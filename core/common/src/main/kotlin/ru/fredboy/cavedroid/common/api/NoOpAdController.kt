package ru.fredboy.cavedroid.common.api

class NoOpAdController : AdController {
    override val supportsPersonalizedAdsConsent: Boolean = false
    override fun showBanner() {}
    override fun hideBanner() {}
    override fun loadInterstitial() {}
    override fun showInterstitial(onDismissed: () -> Unit) {
        onDismissed()
    }
    override fun setPersonalizedAdsEnabled(enabled: Boolean) {}
    override fun resume() {}
    override fun pause() {}
    override fun destroy() {}
}
