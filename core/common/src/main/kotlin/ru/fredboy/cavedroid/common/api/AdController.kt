package ru.fredboy.cavedroid.common.api

interface AdController {

    fun showBanner()

    fun hideBanner()

    fun loadInterstitial()

    fun showInterstitial(onDismissed: () -> Unit)

    fun resume()

    fun pause()

    fun destroy()
}
