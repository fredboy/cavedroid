package ru.fredboy.cavedroid.gdx.menu.v2.view.newgame

sealed interface NewGameMenuState {

    data object Show : NewGameMenuState

    data object Generating : NewGameMenuState
}
