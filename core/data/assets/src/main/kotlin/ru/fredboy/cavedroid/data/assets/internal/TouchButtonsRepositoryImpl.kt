package ru.fredboy.cavedroid.data.assets.internal

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.Rectangle
import kotlinx.serialization.json.Json
import ru.fredboy.cavedroid.data.assets.model.button.TouchButtonsDto
import ru.fredboy.cavedroid.domain.assets.model.TouchButton
import ru.fredboy.cavedroid.domain.assets.repository.TouchButtonsAssetsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TouchButtonsRepositoryImpl @Inject constructor() : TouchButtonsAssetsRepository() {

    private val guiMap = HashMap<String, TouchButton>()

    override fun getTouchButtons(): Map<String, TouchButton> {
        return guiMap
    }

    private fun getMouseKey(name: String): Int {
        return when (name) {
            "Left" -> Input.Buttons.LEFT
            "Right" -> Input.Buttons.RIGHT
            "Middle" -> Input.Buttons.MIDDLE
            "Back" -> Input.Buttons.BACK
            "Forward" -> Input.Buttons.FORWARD
            else -> -1
        }
    }

    private fun loadTouchButtons() {
        val file = Gdx.files.internal(JSON_TOUCH_BUTTONS)
        val buttons = JsonFormat.decodeFromString<TouchButtonsDto>(file.readString())

        buttons.forEach { (name, data) ->
            guiMap[name] = TouchButton(
                rectangle = Rectangle(data.x, data.y, data.width, data.height),
                code = if (data.isMouse) getMouseKey(name) else Input.Keys.valueOf(data.key),
                isMouse = data.isMouse,
            )
        }
    }

    override fun initialize() {
        loadTouchButtons()
    }

    override fun dispose() {
        super.dispose()
        guiMap.clear()
    }

    companion object {
        private val JsonFormat = Json { ignoreUnknownKeys = true }

        private const val JSON_TOUCH_BUTTONS = "json/touch_buttons.json"
    }
}