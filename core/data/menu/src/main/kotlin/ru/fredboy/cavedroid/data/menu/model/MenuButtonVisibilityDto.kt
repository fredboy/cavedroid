package ru.fredboy.cavedroid.data.menu.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuButtonVisibilityDto(
    val android: Boolean = true,
    val desktop: Boolean = true,
    val ios: Boolean = true,
)
