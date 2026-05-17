package ru.fredboy.cavedroid.html

import org.teavm.jso.JSBody
import org.teavm.jso.JSFunctor
import org.teavm.jso.JSObject

@JSFunctor
internal fun interface YaGamesCallback : JSObject {
    fun call()
}

internal object YandexGamesBridge {

    @JvmStatic
    @JSBody(script = "return typeof window !== 'undefined' && !!window.ysdk;")
    external fun isAvailable(): Boolean

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.environment && window.ysdk.environment.i18n) {
                    return window.ysdk.environment.i18n.lang || null;
                }
            } catch (e) {
                console.warn('Reading ysdk.environment.i18n.lang failed:', e);
            }
            return null;
        """,
    )
    external fun getLanguage(): String?

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.features && window.ysdk.features.LoadingAPI) {
                    window.ysdk.features.LoadingAPI.ready();
                }
            } catch (e) {
                console.warn('LoadingAPI.ready() failed:', e);
            }
        """,
    )
    external fun notifyLoadingReady()

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.features && window.ysdk.features.GameplayAPI) {
                    window.ysdk.features.GameplayAPI.start();
                }
            } catch (e) {
                console.warn('GameplayAPI.start() failed:', e);
            }
        """,
    )
    external fun notifyGameplayStart()

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.features && window.ysdk.features.GameplayAPI) {
                    window.ysdk.features.GameplayAPI.stop();
                }
            } catch (e) {
                console.warn('GameplayAPI.stop() failed:', e);
            }
        """,
    )
    external fun notifyGameplayStop()

    @JvmStatic
    @JSBody(
        params = ["callback"],
        script = """
            try {
                if (window.ysdk) {
                    ysdk.on('game_api_pause', callback);
                }
            } catch (e) {
                console.warn('listenGameApiPause(callback) failed:', e);
            }
        """,
    )
    external fun listenGameApiPause(callback: YaGamesCallback)

    @JvmStatic
    @JSBody(
        params = ["callback"],
        script = """
            try {
                if (window.ysdk) {
                    ysdk.on('game_api_resume', callback);
                }
            } catch (e) {
                console.warn('listenGameApiResume(callback) failed:', e);
            }
        """,
    )
    external fun listenGameApiResume(callback: YaGamesCallback)

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.adv) {
                    window.ysdk.adv.showBannerAdv();
                }
            } catch (e) {
                console.warn('showBannerAdv() failed:', e);
            }
        """,
    )
    external fun showBanner()

    @JvmStatic
    @JSBody(
        script = """
            try {
                if (window.ysdk && window.ysdk.adv) {
                    window.ysdk.adv.hideBannerAdv();
                }
            } catch (e) {
                console.warn('hideBannerAdv() failed:', e);
            }
        """,
    )
    external fun hideBanner()

    @JvmStatic
    @JSBody(
        params = ["onClose"],
        script = """
            try {
                if (window.ysdk && window.ysdk.adv) {
                    window.ysdk.adv.showFullscreenAdv({
                        callbacks: {
                            onClose: function () { onClose(); },
                            onError: function (e) { console.warn('Fullscreen ad error:', e); onClose(); }
                        }
                    });
                } else {
                    onClose();
                }
            } catch (e) {
                console.warn('showFullscreenAdv() failed:', e);
                onClose();
            }
        """,
    )
    external fun showInterstitial(onClose: YaGamesCallback)
}
