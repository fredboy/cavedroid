package ru.fredboy.cavedroid.domain.menu.repository

import com.badlogic.gdx.utils.Disposable
import ru.fredboy.cavedroid.domain.menu.model.MenuButton

interface MenuButtonRepository : Disposable {

    fun initialize()

    fun getButtonsForMenu(menuKey: String): Map<String, MenuButton>?

    fun setCurrentMenu(menuKey: String)

    fun getCurrentMenuButtons(): Map<String, MenuButton>?

    fun setButtonEnabled(menuKey: String, buttonKey: String, enabled: Boolean)
}
