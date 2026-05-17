package ru.fredboy.cavedroid.desktop.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.CaveDroidConstants
import ru.fredboy.cavedroid.common.coroutines.GdxMainDispatcher
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.CaveDroidApplicationDecorator
import kotlin.time.Duration.Companion.seconds

class SaveSizePrefsGameDecorator(
    private val delegate: CaveDroidApplication,
) : CaveDroidApplicationDecorator by delegate {

    private val job = Job()
    private val scope = CoroutineScope(job + GdxMainDispatcher)

    private val preferencesStore get() = delegate.getPreferencesStore()

    private val resizeEventFlow = MutableSharedFlow<Pair<Int, Int>>()

    override fun create() {
        delegate.applicationControllerOverride = this
        observeResize()
        delegate.create()
    }

    override fun resize(width: Int, height: Int) {
        scope.launch {
            resizeEventFlow.emit(Pair(width, height))
        }

        delegate.resize(width, height)
    }

    override fun dispose() {
        job.cancel()
        delegate.dispose()
    }

    private fun observeResize() {
        scope.launch {
            resizeEventFlow
                .debounce(1.seconds)
                .collect { (width, height) ->
                    logger.d { "Saving size preference: $width x $height" }
                    withContext(Dispatchers.IO) {
                        preferencesStore.setPreference(
                            key = CaveDroidConstants.PreferenceKeys.WINDOW_WIDTH_KEY,
                            value = width.toString(),
                        )
                        preferencesStore.setPreference(
                            key = CaveDroidConstants.PreferenceKeys.WINDOW_HEIGHT_KEY,
                            value = height.toString(),
                        )
                    }
                }
        }
    }

    companion object {
        private const val TAG = "SaveSizePrefsGameDecorator"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
