package ru.fredboy.cavedroid

import android.app.Activity
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.common.YandexAds
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader
import ru.fredboy.cavedroid.common.api.AdController

class YandexAdController(private val activity: Activity) : AdController {

    @Volatile
    private var shouldShowBanner: Boolean = false

    @Volatile
    private var bannerAdLoaded = false

    override val supportsPersonalizedAdsConsent: Boolean = true

    private val bannerAdView: BannerAdView by lazy {
        val widthDp = (
            activity.resources.displayMetrics.widthPixels /
                activity.resources.displayMetrics.density
            ).toInt() / 2
        BannerAdView(activity).apply {
            setAdSize(BannerAdSize.sticky(activity, widthDp))
            setBannerAdEventListener(object : BannerAdEventListener {
                override fun onAdLoaded() {}
                override fun onAdFailedToLoad(error: AdRequestError) {}
                override fun onAdClicked() {}
                override fun onImpression(data: ImpressionData?) {}
            })
            visibility = View.GONE
        }
    }

    private var bannerAddedToWindow = false
    private var interstitialAd: InterstitialAd? = null
    private val interstitialLoader = InterstitialAdLoader(activity)

    init {
        YandexAds.initialize(activity) {}
    }

    // Deferred until first showBanner() so that libGDX's initialize() has already
    // called setContentView before we attach the banner view on top.
    private fun ensureBannerInWindow() {
        if (bannerAddedToWindow) return
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
        )

        bannerAdView.setBannerAdEventListener(
            object : BannerAdEventListener {
                override fun onAdClicked() = Unit
                override fun onAdFailedToLoad(error: AdRequestError) = Unit
                override fun onImpression(impressionData: ImpressionData?) = Unit
                override fun onAdLoaded() {
                    bannerAdLoaded = true
                    if (shouldShowBanner) {
                        bannerAdView.visibility = View.VISIBLE
                    } else {
                        bannerAdView.visibility = View.GONE
                    }

                    if (!bannerAddedToWindow) {
                        bannerAddedToWindow = true
                        activity.addContentView(bannerAdView, params)
                    }
                }
            },
        )
        bannerAdView.visibility = View.INVISIBLE
        bannerAdView.loadAd(AdRequest.Builder(BuildConfig.BANNER_AD_UNIT_ID).build())
    }

    override fun showBanner() {
        shouldShowBanner = true
        activity.runOnUiThread {
            ensureBannerInWindow()
            if (shouldShowBanner && bannerAdLoaded) {
                bannerAdView.visibility = View.VISIBLE
            }
        }
    }

    override fun hideBanner() {
        shouldShowBanner = false
        activity.runOnUiThread {
            bannerAdView.visibility = View.GONE
        }
    }

    override fun loadInterstitial() {
        if (interstitialAd != null) return
        activity.runOnUiThread {
            interstitialLoader.loadAd(
                AdRequest.Builder(BuildConfig.INTERSTITIAL_AD_UNIT_ID).build(),
                object : InterstitialAdLoadListener {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: AdRequestError) {
                        interstitialAd = null
                    }
                },
            )
        }
    }

    override fun showInterstitial(onDismissed: () -> Unit) {
        activity.runOnUiThread {
            val ad = interstitialAd ?: run {
                onDismissed()
                return@runOnUiThread
            }
            ad.setAdEventListener(object : InterstitialAdEventListener {
                override fun onAdShown() {}

                override fun onAdFailedToShow(error: AdError) {
                    ad.setAdEventListener(null)
                    interstitialAd = null
                    onDismissed()
                }

                override fun onAdDismissed() {
                    ad.setAdEventListener(null)
                    interstitialAd = null
                    loadInterstitial()
                    onDismissed()
                }

                override fun onAdClicked() {}

                override fun onAdImpression(data: ImpressionData?) {}
            })
            ad.show(activity)
        }
    }

    override fun setPersonalizedAdsEnabled(enabled: Boolean) {
        YandexAds.setUserConsent(enabled)
        YandexAds.setAgeRestricted(!enabled)
    }

    override fun resume() {
    }

    override fun pause() {
    }

    override fun destroy() {
        activity.runOnUiThread {
            interstitialAd?.setAdEventListener(null)
            interstitialAd = null
            if (bannerAddedToWindow) bannerAdView.destroy()
        }
    }
}
