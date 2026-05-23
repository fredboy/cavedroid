package ru.fredboy.cavedroid.domain.configuration.model

enum class LightingBackend {
    BFS,
    LEGACY,
    ;

    companion object {
        val DEFAULT: LightingBackend = BFS

        fun fromName(name: String?): LightingBackend {
            if (name.isNullOrBlank()) return DEFAULT
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: DEFAULT
        }
    }
}
