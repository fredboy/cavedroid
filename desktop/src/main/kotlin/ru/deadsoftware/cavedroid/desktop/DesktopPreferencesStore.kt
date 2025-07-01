package ru.deadsoftware.cavedroid.desktop

import ru.fredboy.cavedroid.common.api.PreferencesStore
import java.util.prefs.Preferences

class DesktopPreferencesStore : PreferencesStore {

    private val prefs = Preferences.userNodeForPackage(DesktopPreferencesStore::class.java)

    override fun getPreference(key: String): String? = prefs.get(key, null)

    override fun setPreference(key: String, value: String?) {
        prefs.put(key, value)
    }
}
