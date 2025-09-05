package ru.fredboy.cavedroid.gdx.menu.v2.view.deleteworld

interface DeleteWorldMenuState {

    data object Deleting : DeleteWorldMenuState

    data class ConfirmDeleting(
        val worldName: String,
    ) : DeleteWorldMenuState
}
