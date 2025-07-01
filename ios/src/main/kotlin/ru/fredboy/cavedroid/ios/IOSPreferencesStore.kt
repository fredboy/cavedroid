package ru.fredboy.cavedroid.ios

import org.robovm.apple.foundation.NSUserDefaults
import ru.fredboy.cavedroid.common.api.PreferencesStore

class IOSPreferencesStore : PreferencesStore {

    private val userDefaults = NSUserDefaults.getStandardUserDefaults()

    override fun getPreference(key: String): String? {
        return userDefaults.getString(key)
    }

    override fun setPreference(key: String, value: String?) {
        if (value != null) {
            userDefaults.put(key, value)
        } else {
            userDefaults.remove(key)
        }
        userDefaults.synchronize() // optional, usually not needed unless you want immediate persistence
    }
}
