package ru.fredboy.cavedroid.common

import java.util.Locale

object CaveDroidConstants {
    const val TITLE = "CaveDroid"

    const val VERSION = "1.2.0"

    const val GITHUB_LINK = "https://github.com/fredboy/cavedroid"

    const val MAX_SAVES_COUNT = 8

    object PreferenceKeys {
        const val FULLSCREEN = "fullscreen"

        const val DYNAMIC_CAMERA = "dyncam"

        const val AUTO_JUMP = "auto_jump"

        const val LOCALE = "locale"

        const val SOUND_ENABLED = "sound_enabled"

        const val WINDOW_WIDTH_KEY = "window_width"

        const val WINDOW_HEIGHT_KEY = "window_height"

        const val ONBOARDING_SHOWN = "onboarding_shown"

        const val INVENTORY_HINT_SHOWN = "inventory_hint_shown"

        const val PERSONALIZED_ADS_CONSENT = "personalized_ads_consent"

        const val LIGHTING_BACKEND = "lighting_backend"
    }

    val SUPPORTED_LOCALES = listOf(Locale("en"), Locale("es"), Locale("pt"), Locale("de"), Locale("ru"))
}
