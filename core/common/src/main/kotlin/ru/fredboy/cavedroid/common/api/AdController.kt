package ru.fredboy.cavedroid.common.api

interface AdController {

    val supportsPersonalizedAdsConsent: Boolean

    fun showBanner()

    fun hideBanner()

    fun loadInterstitial()

    fun showInterstitial(onDismissed: () -> Unit)

    fun setPersonalizedAdsEnabled(enabled: Boolean)

    fun resume()

    fun pause()

    fun destroy()
}
