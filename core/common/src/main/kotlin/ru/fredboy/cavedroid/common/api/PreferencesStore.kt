package ru.fredboy.cavedroid.common.api

interface PreferencesStore {

    fun getPreference(key: String): String?

    fun setPreference(key: String, value: String?)
}
