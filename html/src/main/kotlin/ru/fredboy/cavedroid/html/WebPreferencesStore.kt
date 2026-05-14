package ru.fredboy.cavedroid.html

import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.api.PreferencesStore

class WebPreferencesStore : PreferencesStore {

    private val prefs by lazy { Gdx.app.getPreferences(PREFS_NAME) }

    override fun getPreference(key: String): String? {
        return if (prefs.contains(key)) prefs.getString(key) else null
    }

    override fun setPreference(key: String, value: String?) {
        if (value == null) {
            prefs.remove(key)
        } else {
            prefs.putString(key, value)
        }
        prefs.flush()
    }

    private companion object {
        const val PREFS_NAME = "cavedroid"
    }
}
