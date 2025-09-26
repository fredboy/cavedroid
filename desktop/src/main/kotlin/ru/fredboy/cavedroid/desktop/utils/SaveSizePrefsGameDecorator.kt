package ru.fredboy.cavedroid.desktop.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.fredboy.cavedroid.common.CaveDroidConstants
import ru.fredboy.cavedroid.gdx.CaveDroidApplication
import ru.fredboy.cavedroid.gdx.CaveDroidApplicationDecorator

class SaveSizePrefsGameDecorator(
    private val delegate: CaveDroidApplication,
) : CaveDroidApplicationDecorator by delegate {

    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.Default)

    private val preferencesStore get() = delegate.getPreferencesStore()

    override fun resize(width: Int, height: Int) {
        scope.launch {
            logger.d { "Saving size preference: $width x $height" }
            withContext(Dispatchers.IO) {
                preferencesStore.setPreference(CaveDroidConstants.PreferenceKeys.WINDOW_WIDTH_KEY, width.toString())
                preferencesStore.setPreference(CaveDroidConstants.PreferenceKeys.WINDOW_HEIGHT_KEY, height.toString())
            }
        }

        delegate.resize(width, height)
    }

    override fun dispose() {
        job.cancel()
        delegate.dispose()
    }

    companion object {
        private const val TAG = "SaveSizePrefsGameDecorator"
        private val logger = co.touchlab.kermit.Logger.withTag(TAG)
    }
}
