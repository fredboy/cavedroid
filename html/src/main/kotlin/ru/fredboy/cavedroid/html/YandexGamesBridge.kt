package ru.fredboy.cavedroid.html

import org.teavm.jso.JSBody
import org.teavm.jso.JSFunctor
import org.teavm.jso.JSObject

@JSFunctor
internal fun interface YaGamesCallback : JSObject {
    fun call()
}

@JSFunctor
internal fun interface YaGamesStringCallback : JSObject {
    fun call(value: String?)
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

    @JvmStatic
    @JSBody(
        params = ["callback"],
        script = """
            try {
                if (window.ysdk && typeof window.ysdk.getPlayer === 'function') {
                    window.ysdk.getPlayer().then(function (player) {
                        window.__cavedroidPlayer = player;
                        callback();
                    }).catch(function (err) {
                        console.warn('ysdk.getPlayer() failed:', err);
                        window.__cavedroidPlayer = null;
                        callback();
                    });
                } else {
                    window.__cavedroidPlayer = null;
                    callback();
                }
            } catch (e) {
                console.warn('initPlayer() failed:', e);
                window.__cavedroidPlayer = null;
                callback();
            }
        """,
    )
    external fun initPlayer(callback: YaGamesCallback)

    @JvmStatic
    @JSBody(
        script = """
            try {
                return !!(window.__cavedroidPlayer &&
                    typeof window.__cavedroidPlayer.isAuthorized === 'function' &&
                    window.__cavedroidPlayer.isAuthorized());
            } catch (e) {
                console.warn('isPlayerAuthorized() failed:', e);
                return false;
            }
        """,
    )
    external fun isPlayerAuthorized(): Boolean

    @JvmStatic
    @JSBody(
        params = ["statsJson"],
        script = """
            try {
                if (window.__cavedroidPlayer &&
                    typeof window.__cavedroidPlayer.setStats === 'function') {
                    window.__cavedroidPlayer.setStats(JSON.parse(statsJson));
                }
            } catch (e) {
                console.warn('setStats() failed:', e);
            }
        """,
    )
    external fun setStats(statsJson: String)

    @JvmStatic
    @JSBody(
        params = ["callback"],
        script = """
            try {
                if (window.__cavedroidPlayer &&
                    typeof window.__cavedroidPlayer.getStats === 'function') {
                    window.__cavedroidPlayer.getStats().then(function (s) {
                        callback(JSON.stringify(s || {}));
                    }).catch(function (err) {
                        console.warn('getStats() failed:', err);
                        callback(null);
                    });
                } else {
                    callback(null);
                }
            } catch (e) {
                console.warn('getStats() failed:', e);
                callback(null);
            }
        """,
    )
    external fun getStats(callback: YaGamesStringCallback)

    @JvmStatic
    @JSBody(
        params = ["name", "score"],
        script = """
            try {
                if (window.ysdk && window.ysdk.leaderboards) {
                    window.ysdk.leaderboards.setScore(name, score);
                }
            } catch (e) {
                console.warn('setLeaderboardScore(' + name + ') failed:', e);
            }
        """,
    )
    external fun setLeaderboardScore(name: String, score: Double)

    @JvmStatic
    @JSBody(
        params = ["name", "callback"],
        script = """
            try {
                if (window.ysdk && window.ysdk.leaderboards) {
                    window.ysdk.leaderboards.getPlayerEntry(name).then(function (entry) {
                        callback(JSON.stringify(entry || null));
                    }).catch(function (err) {
                        if (err && err.code === 'LEADERBOARD_PLAYER_NOT_PRESENT') {
                            callback(null);
                            return;
                        }
                        console.warn('getPlayerEntry(' + name + ') failed:', err);
                        callback(null);
                    });
                } else {
                    callback(null);
                }
            } catch (e) {
                console.warn('getPlayerEntry(' + name + ') failed:', e);
                callback(null);
            }
        """,
    )
    external fun getLeaderboardPlayerEntry(name: String, callback: YaGamesStringCallback)
}
