package ru.fredboy.cavedroid.data.menu.model

import kotlinx.serialization.Serializable

@Serializable
data class MenuButtonDto(
    val label: String? = null,
    val actionKey: String? = null,
    val type: String? = null,
    val enabled: Boolean? = null,
    val visibility: MenuButtonVisibilityDto? = null,
    val options: List<String>? = null,
)
