package ru.fredboy.cavedroid.data.menu.repository

import com.badlogic.gdx.Gdx
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.common.di.MenuScope
import ru.fredboy.cavedroid.data.menu.mapper.MenuButtonMapper
import ru.fredboy.cavedroid.data.menu.model.MenuButtonDto
import ru.fredboy.cavedroid.domain.menu.model.MenuButton
import ru.fredboy.cavedroid.domain.menu.repository.MenuButtonRepository
import javax.inject.Inject

@MenuScope
class MenuButtonRepositoryImpl @Inject constructor(
    private val menuButtonMapper: MenuButtonMapper,
) : MenuButtonRepository {

    private var _initialized = false

    private val menuMap = LinkedHashMap<String, MutableMap<String, MenuButton>>()

    private var currentMenu = "main"

    init {
        initialize()
    }

    override fun initialize() {
        if (_initialized) {
            Gdx.app.debug(TAG, "Attempted to init when already initialized")
            return
        }

        val jsonString = Gdx.files.internal("json/menu.json").readString()

        JsonFormat.decodeFromString<Map<String, Map<String, MenuButtonDto>>>(jsonString)
            .forEach { (key, value) ->
                menuMap[key] = value.mapValues { (_, dto) -> menuButtonMapper.map(dto) }.toMutableMap()
            }

        _initialized = true
    }

    override fun getButtonsForMenu(menuKey: String): Map<String, MenuButton>? {
        require(_initialized) { "$TAG was not initialized before access!" }

        return menuMap[menuKey].also {
            if (it == null) {
                Gdx.app.debug(TAG, "No buttons for $menuKey menu found")
            }
        }
    }

    override fun setCurrentMenu(menuKey: String) {
        currentMenu = menuKey
    }

    override fun getCurrentMenuButtons(): Map<String, MenuButton>? {
        require(_initialized) { "$TAG was not initialized before access!" }

        return menuMap[currentMenu]?.filterValues { button -> button.isVisible }
    }

    override fun setButtonEnabled(menuKey: String, buttonKey: String, enabled: Boolean) {
        val menu = menuMap[menuKey] ?: run {
            Gdx.app.error(TAG, "setButtonEnabled: menu with key '$menuKey' not found")
            return
        }

        val button = menu[buttonKey] ?: run {
            Gdx.app.error(TAG, "setButtonEnabled: button with key '$buttonKey' not found")
            return
        }

        menu[buttonKey] = menuButtonMapper.setButtonEnabled(button, enabled)
    }

    override fun dispose() {
        menuMap.clear()
        _initialized = false
    }

    companion object {
        private const val TAG = "MenuButtonsRepositoryImpl"

        private val JsonFormat = Json { ignoreUnknownKeys = true }
    }
}
