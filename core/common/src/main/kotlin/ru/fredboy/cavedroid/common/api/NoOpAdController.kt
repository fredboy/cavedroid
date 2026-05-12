package ru.fredboy.cavedroid.common.api

class NoOpAdController : AdController {
    override fun showBanner() {}
    override fun hideBanner() {}
    override fun loadInterstitial() {}
    override fun showInterstitial(onDismissed: () -> Unit) { onDismissed() }
    override fun resume() {}
    override fun pause() {}
    override fun destroy() {}
}
