package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

sealed interface NewGameMenuState {

    data class Show(val isKeyboardUp: Boolean = false) : NewGameMenuState
}
