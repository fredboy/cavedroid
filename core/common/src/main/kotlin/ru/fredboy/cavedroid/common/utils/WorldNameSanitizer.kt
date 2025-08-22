package ru.fredboy.cavedroid.common.utils

import dagger.Reusable
import javax.inject.Inject

@Reusable
class WorldNameSanitizer @Inject constructor() {

    fun sanitizeWorldName(input: String): String {
        var safe = input.replace(Regex("[^a-zA-Z0-9 _.-]"), "_")

        safe = safe.trim().trim('.', '_')

        safe = safe.replace(Regex("_+"), "_")

        if (WINDOWS_RESERVED.contains(safe.uppercase())) {
            safe = "_$safe"
        }

        if (safe.length > 64) {
            safe = safe.take(64)
        }

        if (safe.isBlank()) {
            safe = "world"
        }

        return safe
    }

    companion object {
        private val REGEX = Regex("[^a-zA-Z0-9 _-]")

        private val WINDOWS_RESERVED = setOf(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9",
        )
    }
}
