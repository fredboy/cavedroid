package ru.fredboy.cavedroid.gdx.menu.v2.view.editworld

sealed interface EditWorldMenuState {

    data object Loading : EditWorldMenuState

    data class ShowInfo(
        val details: SaveDetailsVo,
        val statusMessage: String? = null,
    ) : EditWorldMenuState

    data class Renaming(
        val currentName: String,
        val isKeyboardUp: Boolean,
    ) : EditWorldMenuState

    data class ConfirmDelete(
        val worldName: String,
    ) : EditWorldMenuState

    data class Working(
        val message: String,
    ) : EditWorldMenuState
}
