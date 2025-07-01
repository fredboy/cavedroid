package ru.fredboy.cavedroid.data.menu.mapper

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.data.menu.model.MenuButtonDto
import ru.fredboy.cavedroid.data.menu.model.MenuButtonVisibilityDto
import ru.fredboy.cavedroid.domain.menu.model.MenuButton
import javax.inject.Inject

@MenuScope
class MenuButtonMapper @Inject constructor() {

    fun map(dto: MenuButtonDto): MenuButton = when (dto.type) {
        "boolean_option" -> MenuButton.BooleanOption(
            label = dto.label.toString(),
            isVisible = mapVisibility(dto.visibility),
            actionKey = dto.actionKey.orEmpty(),
            optionKeys = dto.options.orEmpty(),
            isEnabled = dto.enabled ?: true,
        )

        "default", null -> MenuButton.Simple(
            label = dto.label.toString(),
            isVisible = mapVisibility(dto.visibility),
            actionKey = dto.actionKey.orEmpty(),
            isEnabled = dto.enabled ?: true,
        )

        else ->
            throw IllegalArgumentException("Unknown button type: ${dto.type}")
    }

    fun setButtonEnabled(button: MenuButton, isEnabled: Boolean): MenuButton = when (button) {
        is MenuButton.Simple -> button.copy(isEnabled = isEnabled)
        is MenuButton.BooleanOption -> button.copy(isEnabled = isEnabled)
    }

    private fun mapVisibility(dto: MenuButtonVisibilityDto?): Boolean {
        dto ?: return true

        return when (Gdx.app.type) {
            Application.ApplicationType.Android -> dto.android
            else -> dto.desktop
        }
    }
}
