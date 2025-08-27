package ru.fredboy.cavedroid.gdx.menu.v2.view.singleplayer

sealed interface SinglePlayerMenuState {

    data object LoadingWorld : SinglePlayerMenuState

    data class ShowList(
        val saves: List<SaveInfoVo>,
    ) : SinglePlayerMenuState
}
