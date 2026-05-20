package ru.fredboy.cavedroid.gdx.menu.v2.view.worldconfig

sealed interface WorldConfigMenuState {

    data object Show : WorldConfigMenuState

    data object Generating : WorldConfigMenuState
}
