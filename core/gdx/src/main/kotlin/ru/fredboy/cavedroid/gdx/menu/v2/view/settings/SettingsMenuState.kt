package ru.fredboy.cavedroid.gdx.menu.v2.view.settings

data class SettingsMenuState(
    val dynamicCamera: Boolean,
    val fullscreen: Boolean,
    val autoJump: Boolean,
    val sound: Boolean,
    val smoothLighting: Boolean,
    val showPersonalizedAdsToggle: Boolean,
    val personalizedAds: Boolean,
    val showFullscreenButton: Boolean,
    val canResetHints: Boolean,
    val showDebugSetting: Boolean,
    val preferShowDebug: Boolean,
)
