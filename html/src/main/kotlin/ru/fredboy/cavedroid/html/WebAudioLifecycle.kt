package ru.fredboy.cavedroid.html

import org.teavm.jso.JSBody

internal object WebAudioLifecycle {

    fun install(onSuspend: YaGamesCallback, onResume: YaGamesCallback) {
        registerListeners(onSuspend, onResume)
    }

    // `visibilitychange` covers desktop tab switches and most Android browsers.
    // `pagehide`/`pageshow` are the only reliable signals for iOS Safari swap-out
    // and "page being closed" on mobile (where `unload` does not fire).
    // `blur`/`focus` catch window minimize and out-of-tab focus loss.
    @JvmStatic
    @JSBody(
        params = ["onSuspend", "onResume"],
        script = """
            try {
                var suspend = function () { try { onSuspend(); } catch (e) { console.warn('[cavedroid] onSuspend failed:', e); } };
                var resume = function () { try { onResume(); } catch (e) { console.warn('[cavedroid] onResume failed:', e); } };
                document.addEventListener('visibilitychange', function () {
                    if (document.visibilityState === 'hidden') { suspend(); } else { resume(); }
                });
                window.addEventListener('pagehide', suspend);
                window.addEventListener('pageshow', resume);
                window.addEventListener('blur', suspend);
                window.addEventListener('focus', resume);
            } catch (e) {
                console.warn('[cavedroid] WebAudioLifecycle install failed:', e);
            }
        """,
    )
    external fun registerListeners(onSuspend: YaGamesCallback, onResume: YaGamesCallback)
}
