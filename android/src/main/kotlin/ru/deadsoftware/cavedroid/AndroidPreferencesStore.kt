package ru.deadsoftware.cavedroid

import android.content.Context
import ru.fredboy.cavedroid.common.api.PreferencesStore

class AndroidPreferencesStore(
    private val context: Context
) : PreferencesStore {

    private val sharedPreferences by lazy { context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE) }

    override fun getPreference(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun setPreference(key: String, value: String?) {
        with(sharedPreferences.edit()) {
            putString(key, value)
            apply()
        }
    }

    private companion object {
        private const val SHARED_PREFS_NAME = "cavedroid_prefs"
    }
}