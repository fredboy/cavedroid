package ru.deadsoftware.cavedroid.prefs

interface PreferencesStore {

    fun getPreference(key: String): String?

    fun setPreference(key: String, value: String?)

}
